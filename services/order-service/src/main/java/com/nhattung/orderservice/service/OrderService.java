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
import com.nhattung.orderservice.request.SelectedCartItemRequest;
import com.nhattung.orderservice.utils.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
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
                .orderDate(LocalDate.now())
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
        orderDto.setItems(convertToOrderItemDtoList(order));
        return orderDto;
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
