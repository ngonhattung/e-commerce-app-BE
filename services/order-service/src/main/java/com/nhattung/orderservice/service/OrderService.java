package com.nhattung.orderservice.service;


import com.nhattung.orderservice.dto.CartDto;
import com.nhattung.orderservice.dto.ItemInCartDto;
import com.nhattung.orderservice.dto.OrderDto;
import com.nhattung.orderservice.entity.Order;
import com.nhattung.orderservice.entity.OrderItem;
import com.nhattung.orderservice.enums.OrderStatus;
import com.nhattung.orderservice.exception.AppException;
import com.nhattung.orderservice.exception.ErrorCode;
import com.nhattung.orderservice.repository.OrderRepository;
import com.nhattung.orderservice.repository.httpclient.CartClient;
import com.nhattung.orderservice.request.SelectedCartItemRequest;
import com.nhattung.orderservice.utils.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService implements IOrderService{

    private final OrderRepository orderRepository;
    private final AuthenticatedUser authenticatedUser;
    private final CartClient cartClient;
    private final ModelMapper modelMapper;
    @Override
    public Order placeOrder(SelectedCartItemRequest request) {
        CartDto cart = cartClient.getCart();
        var cartItems = cart.getItems()
                .stream()
                .filter(cartItem -> request.getSelectedCartItemIds().contains(cartItem.getId()))
                .toList();
        if(cartItems.isEmpty()){
            throw new AppException(ErrorCode.CART_ITEM_NOT_FOUND);
        }
        Order order = createOrder();
        List<OrderItem> orderItems = createOrderItems(order, cartItems);
        order.setOrderItems(new HashSet<>(orderItems));
        order.setTotalAmount(calculateTotalAmount(orderItems));

        return orderRepository.save(order);
    }

    private Order createOrder() {
        return Order.builder()
                .userId(authenticatedUser.getUserId())
                .orderStatus(OrderStatus.ORDER_CREATED)
                .orderDate(LocalDate.now())
                .promotionId(1L)
                .build();
    }

    private List<OrderItem> createOrderItems(Order order, List<ItemInCartDto> selectedItems) {
        return selectedItems
                .stream()
                .map(item -> OrderItem.builder()
                        .order(order)
                        .productId(item.getProductId())
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
        return modelMapper.map(order, OrderDto.class);
    }
}
