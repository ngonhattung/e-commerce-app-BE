package com.nhattung.orderservice.service;


import com.nhattung.orderservice.dto.*;
import com.nhattung.orderservice.entity.Order;
import com.nhattung.orderservice.entity.OrderItem;
import com.nhattung.orderservice.enums.OrderStatus;
import com.nhattung.orderservice.exception.AppException;
import com.nhattung.orderservice.exception.ErrorCode;
import com.nhattung.orderservice.repository.OrderRepository;
import com.nhattung.orderservice.repository.httpclient.CartClient;
import com.nhattung.orderservice.repository.httpclient.PromotionClient;
import com.nhattung.orderservice.request.SelectedCartItemRequest;
import com.nhattung.orderservice.utils.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class OrderService implements IOrderService{

    private final OrderRepository orderRepository;
    private final AuthenticatedUser authenticatedUser;
    private final CartClient cartClient;
    private final PromotionClient promotionClient;
    private final ModelMapper modelMapper;
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
        PromotionDto promotion = promotionClient.getActivePromotionByCode(request.getCouponCode()).getResult();
        order.setPromotionId(promotion.getId());
        BigDecimal totalAmount = calculateTotalAmount(createOrderItems(order, cartItems));
        BigDecimal finalAmount = getFinalAmount(promotion, totalAmount);
        List<OrderItem> orderItems = createOrderItems(order, cartItems);
        order.setOrderItems(new HashSet<>(orderItems));
        order.setTotalAmount(finalAmount);

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
                .userId(authenticatedUser.getUserId())
                .orderStatus(OrderStatus.ORDER_CREATED)
                .orderDate(LocalDate.now())
                .build();
    }

    private List<OrderItem> createOrderItems(Order order, List<CartItemDto> selectedItems) {
        return selectedItems
                .stream()
                .map(item -> OrderItem.builder()
                        .order(order)
                        .productId(item.getProduct().getId())
                        .quantity(item.getQuantity())
                        .price(item.getUnitPrice())
                        .build())
                .toList();
    }
    private BigDecimal calculateTotalAmount(List<OrderItem> orderItemList) {
        return orderItemList
                .stream()
                .map(orderItem -> orderItem.getPrice()
                        .multiply(BigDecimal.valueOf(orderItem.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    @Override
    public OrderDto getOrder(Long orderId) {
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

    private OrderItemDto convertToOrderItemDto(OrderItem orderItem) {
        OrderItemDto orderItemDto = modelMapper.map(orderItem, OrderItemDto.class);
        ProductDto productDto = cartClient.getCart()
                .getResult()
                .getItems()
                .stream()
                .map(CartItemDto::getProduct)
                .filter(product -> product.getId().equals(orderItem.getProductId()))
                .findFirst()
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));
        orderItemDto.setProduct(productDto);
        return orderItemDto;
    }
    private List<OrderItemDto> convertToOrderItemDtoList(Order order) {
        return order.getOrderItems()
                .stream()
                .map(this::convertToOrderItemDto)
                .toList();
    }
}
