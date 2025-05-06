package com.nhattung.orderservice.service;


import com.nhattung.dto.OrderItemSagaDto;
import com.nhattung.dto.OrderSagaDto;
import com.nhattung.enums.OrderStatus;
import com.nhattung.event.dto.OrderSagaEvent;
import com.nhattung.orderservice.dto.*;
import com.nhattung.orderservice.entity.Order;
import com.nhattung.orderservice.entity.OrderItem;
import com.nhattung.orderservice.exception.AppException;
import com.nhattung.orderservice.exception.ErrorCode;
import com.nhattung.orderservice.repository.OrderRepository;
import com.nhattung.orderservice.repository.httpclient.CartClient;
import com.nhattung.orderservice.repository.httpclient.ProductClient;
import com.nhattung.orderservice.repository.httpclient.PromotionClient;
import com.nhattung.orderservice.repository.httpclient.UserClient;
import com.nhattung.orderservice.request.PageResponse;
import com.nhattung.orderservice.request.SelectedCartItemRequest;
import com.nhattung.orderservice.utils.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService implements IOrderService{

    private final OrderRepository orderRepository;
    private final AuthenticatedUser authenticatedUser;
    private final CartClient cartClient;
    private final PromotionClient promotionClient;
    private final ModelMapper modelMapper;
    private final KafkaTemplate<String, OrderSagaEvent> kafkaTemplate;
    private final ProductClient productClient;
    private final UserClient userClient;
    @Override
    public Order placeOrder(SelectedCartItemRequest request) {
        CartDto cart = cartClient.getCart().getResult();
        var cartItems = cart.getItems()
                .stream()
                .filter(cartItem -> request.getSelectedCartItemIds().contains(cartItem.getId()))
                .toList();
        if(cartItems.isEmpty()){
            throw new AppException(ErrorCode.CART_ITEM_NOT_FOUND);
        }
        Order order = createOrder();
        order.setOrderStatus(OrderStatus.ORDER_CREATED);
        order.setShippingAddress(request.getShippingAddress());
        PromotionDto promotion = promotionClient.getActivePromotionByCode(request.getCouponCode()).getResult();
        order.setPromotionId(promotion.getId());

        List<OrderItemDto> orderItems = createOrderItems(cartItems);
        BigDecimal totalAmount = calculateTotalAmount(orderItems);
        BigDecimal finalAmount = getFinalAmount(promotion, totalAmount);


        cartClient.deleteItemsOrder(request.getSelectedCartItemIds());

        //Map orderItems to OrderItem
        var orderItemsOrigin = orderItems
                .stream()
                .map(orderItem -> OrderItem.builder()
                        .id(orderItem.getId())
                        .productId(orderItem.getProduct().getId())
                        .quantity(orderItem.getQuantity())
                        .price(orderItem.getPrice())
                        .order(order)
                        .build())
                .toList();

        order.setOrderItems(new HashSet<>(orderItemsOrigin));
        order.setTotalAmount(finalAmount);

        OrderSagaDto orderSagaDto = OrderSagaDto.builder()
                .orderId(order.getId())
                .userId(authenticatedUser.getUserId())
                .totalPrice(finalAmount)
                .email(authenticatedUser.getEmail())
                .shippingAddress(request.getShippingAddress())
                .promotionId(promotion.getId())
                .isGlobalPromotion(promotion.getIsGlobal())
                .orderItems(orderItems
                        .stream()
                        .map(item -> OrderItemSagaDto.builder()
                                .orderItemId(item.getId())
                                .productId(item.getProduct().getId())
                                .productName(item.getProduct().getName())
                                .quantity(item.getQuantity())
                                .price(item.getPrice())
                                .build())
                        .collect(Collectors.toSet()))
                .build();

        OrderSagaEvent orderSagaEvent = OrderSagaEvent.builder()
                .order(orderSagaDto)
                .orderStatus(OrderStatus.ORDER_CREATED)
                .message("Order created")
                .build();
        kafkaTemplate.send("order-created-topic", orderSagaEvent);
        return orderRepository.save(order);
    }

    private BigDecimal getFinalAmount(PromotionDto promotion, BigDecimal totalAmount) {
        if(promotion.getMinimumOrderValue().compareTo(totalAmount) > 0) {
            throw new AppException(ErrorCode.PROMOTION_MINIMUM_ORDER_VALUE_NOT_MET);
        }
        BigDecimal percentDiscount = totalAmount
                    .multiply(promotion.getDiscountPercent())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal totalAmountWithPromotion = promotion.getDiscountAmount().max(percentDiscount);

        return totalAmount.subtract(totalAmountWithPromotion);
    }


    private Order createOrder() {
        return Order.builder()
                .id(UUID.randomUUID().toString())
                .userId(authenticatedUser.getUserId())
                .orderDate(LocalDateTime.now())
                .build();
    }

    private List<OrderItemDto> createOrderItems(List<CartItemDto> selectedItems) {
        return selectedItems
                .stream()
                .map(item -> OrderItemDto.builder()
                        .product(item.getProduct())
                        .quantity(item.getQuantity())
                        .price(item.getUnitPrice())
                        .build())
                .toList();
    }
    private BigDecimal calculateTotalAmount(List<OrderItemDto> orderItemList) {
        return orderItemList
                .stream()
                .map(orderItem -> orderItem.getPrice()
                        .multiply(BigDecimal.valueOf(orderItem.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    @Override
    public OrderDto getOrder(String orderId) {
        return orderRepository.findById(orderId)
                .map(this::convertToDto)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));
    }

    @Override
    public List<OrderDto> getOrdersByUserId() {
        return orderRepository.findByUserId(authenticatedUser.getUserId())
                .stream()
                .map(this::convertToDto)
                .toList();
    }
    @Override
    public OrderDto convertToDto(Order order) {
        OrderDto orderDto =  modelMapper.map(order, OrderDto.class);
        orderDto.setUser(userClient.getUserProfileById(order.getUserId()).getResult());
        orderDto.setItems(convertToOrderItemDtoList(order));
        return orderDto;
    }

    @Override
    public PageResponse<OrderDto> getAllOrders(int page, int size) {
        if(page < 0 || size < 1) {
            throw new AppException(ErrorCode.INVALID_PAGE_SIZE);
        }

        Sort sort = Sort.by(Sort.Direction.DESC, "orderDate");
        Pageable pageable = PageRequest.of(page - 1, size, sort);

        Page<Order> orderPage = orderRepository.findAll(pageable);
        List<OrderDto> orderDtos = orderPage.getContent()
                .stream()
                .map(this::convertToDto)
                .toList();
        return PageResponse.<OrderDto>builder()
                .currentPage(page)
                .totalPages(orderPage.getTotalPages())
                .totalElements(orderPage.getTotalElements())
                .pageSize(size)
                .data(orderDtos)
                .build();
    }

    @Override
    public long countOrders() {
        return orderRepository.count();
    }

    @Override
    public BigDecimal getTotalRevenue() {

        return orderRepository.getTotalRevenue()
                .setScale(2, RoundingMode.HALF_UP);

    }

    @Override
    public List<RevenueDto> getRevenueByTimeRange(String timeRange) {
        List<Map<String, Object>> results = switch (timeRange.toLowerCase()) {
            case "weekly" -> orderRepository.getWeeklyRevenue();
            case "monthly" -> orderRepository.getMonthlyRevenue();
            default -> orderRepository.getDailyRevenue();
        };

        return mapToRevenueDto(results);
    }

    @Override
    public List<CategoryRevenueDto> getRevenueByCategory() {

        // 1. Lấy doanh thu theo product_id
        List<ProductRevenueDto> productRevenues = mapToCategoryRevenueDto(orderRepository.getRevenueByProduct());


        // 2. Chuyển đổi danh sách product_id thành danh sách duy nhất để giảm số lần gọi API
        List<Long> productIds = productRevenues.stream()
                .map(ProductRevenueDto::getProductId)
                .collect(Collectors.toList());

        // 3. Gọi product service để lấy thông tin category cho mỗi product
        Map<Long, ProductDto> productInfoMap = productClient.getProductsByIds(productIds)
                .getResult()
                .stream()
                .collect(Collectors.toMap(ProductDto::getId, productDto -> productDto));

        // 4. Nhóm doanh thu theo category
        Map<String, BigDecimal> categoryRevenueMap = new HashMap<>();

        for (ProductRevenueDto productRevenue : productRevenues) {
            ProductDto productInfo = productInfoMap.get(productRevenue.getProductId());
            if (productInfo != null) {
                String categoryName = productInfo.getCategory().getName();
                BigDecimal revenue = productRevenue.getRevenue();

                categoryRevenueMap.merge(categoryName, revenue, BigDecimal::add);
            }
        }

        // 5. Chuyển đổi map thành danh sách kết quả và sắp xếp
        return categoryRevenueMap.entrySet().stream()
                .map(entry -> new CategoryRevenueDto(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparing(CategoryRevenueDto::getCategoryName))
                .collect(Collectors.toList());

    }

    @Override
    public List<TopProductDto> getTopSellingProducts() {
        List<ProductSalesDto> topProducts = orderRepository.getTopSellingProducts()
                .stream()
                .map(result -> {
                    Object productIdObj = result.get("productId");
                    Object soldObj = result.get("sold");

                    // Safe conversion for productId
                    Long productId = (productIdObj instanceof BigDecimal)
                            ? ((BigDecimal) productIdObj).longValue()
                            : (Long) productIdObj;

                    // Safe conversion for sold
                    Long sold = (soldObj instanceof BigDecimal)
                            ? ((BigDecimal) soldObj).longValue()
                            : (Long) soldObj;

                    return new ProductSalesDto(
                            productId,
                            (BigDecimal) result.get("revenue"),
                            (BigDecimal) result.get("price"),
                            sold
                    );
                }).toList();

        // Lấy product IDs
        List<Long> productIds = topProducts.stream()
                .map(ProductSalesDto::getProductId)
                .collect(Collectors.toList());

        Map<Long, ProductDto> productInfoMap = productClient.getProductsByIds(productIds)
                .getResult()
                .stream()
                .collect(Collectors.toMap(ProductDto::getId, productDto -> productDto));


        return topProducts.stream()
                .map(productSalesDto -> {
                    ProductDto productInfo = productInfoMap.get(productSalesDto.getProductId());
                    if (productInfo != null) {
                        String imageUri = productInfo.getImages() != null && !productInfo.getImages().isEmpty()
                                ? productInfo.getImages().getFirst().getFileUri()
                                : null;
                        return TopProductDto.builder()
                                .productId(productSalesDto.getProductId())
                                .productName(productInfo.getName())
                                .productImage(imageUri)
                                .quantity(productInfo.getQuantity())
                                .price(productSalesDto.getPrice())
                                .sold(productSalesDto.getSold())
                                .revenue(productSalesDto.getRevenue())
                                .categoryName(productInfo.getCategory().getName())
                                .build();
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(TopProductDto::getSold).reversed())
                .collect(Collectors.toList());


    }

    private List<ProductRevenueDto> mapToCategoryRevenueDto(List<Map<String, Object>> results) {
        return results.stream()
                .map(result -> new ProductRevenueDto(
                        (Long) result.get("productId"),
                        ((BigDecimal) result.get("revenue"))
                ))
                .collect(Collectors.toList());
    }

    private List<RevenueDto> mapToRevenueDto(List<Map<String, Object>> results) {
        return results.stream()
                .map(result -> {
                    Object dateObj = result.get("date");
                    String dateStr;

                    // Handle different date types
                    if (dateObj instanceof java.sql.Date) {
                        dateStr = ((java.sql.Date) dateObj).toString();  // Converts to YYYY-MM-DD format
                    } else if (dateObj instanceof java.sql.Timestamp) {
                        dateStr = ((java.sql.Timestamp) dateObj).toString();
                    } else {
                        dateStr = (String) dateObj;
                    }

                    return new RevenueDto(
                            dateStr,
                            ((BigDecimal) result.get("revenue"))
                    );
                })
                .collect(Collectors.toList());
    }


    private List<OrderItemDto> convertToOrderItemDtoList(Order order) {
        List<ProductDto> productDtos = productClient.getProductsByIds(
                order.getOrderItems()
                        .stream()
                        .map(OrderItem::getProductId)
                        .toList()).getResult();

        return order.getOrderItems()
                .stream()
                .map(orderItem -> {
                    ProductDto productDto = productDtos
                            .stream()
                            .filter(product -> product.getId().equals(orderItem.getProductId()))
                            .findFirst()
                            .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));
                    return OrderItemDto.builder()
                            .id(orderItem.getId())
                            .product(productDto)
                            .quantity(orderItem.getQuantity())
                            .price(orderItem.getPrice())
                            .build();
                })
                .toList();
    }
}
