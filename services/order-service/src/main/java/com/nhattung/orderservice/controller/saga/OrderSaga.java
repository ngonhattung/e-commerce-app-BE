package com.nhattung.orderservice.controller.saga;

import com.nhattung.enums.OrderStatus;
import com.nhattung.event.dto.OrderSagaEvent;
import com.nhattung.orderservice.dto.OrderDto;
import com.nhattung.orderservice.entity.Order;
import com.nhattung.orderservice.exception.AppException;
import com.nhattung.orderservice.exception.ErrorCode;
import com.nhattung.orderservice.repository.OrderRepository;
import com.nhattung.orderservice.service.IOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderSaga {

    private final OrderRepository orderRepository;
    private final KafkaTemplate<String, OrderSagaEvent> kafkaTemplate;


    @KafkaListener(topics = "inventory-checkingResponse-topic")
    @Transactional
    public void handleInventoryChecked(OrderSagaEvent orderSagaEvent)
    {
        log.info("Nhận phản hồi từ Inventory Service: {}", orderSagaEvent);

        Order order = orderRepository.findById(orderSagaEvent.getOrder().getOrderId())
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        if(orderSagaEvent.getOrderStatus() == OrderStatus.INVENTORY_CHECKED)
        {
            order.setOrderStatus(OrderStatus.PAYMENT_PROCESSING);
            orderRepository.save(order);

            orderSagaEvent.setOrderStatus(OrderStatus.PAYMENT_PROCESSING);
            orderSagaEvent.setMessage("Inventory checked, processing payment");
            kafkaTemplate.send("payment-processing-topic", orderSagaEvent);
        } else if (orderSagaEvent.getOrderStatus() == OrderStatus.INVENTORY_FAILED) {

            order.setOrderStatus(OrderStatus.ORDER_CANCELLED);
            orderRepository.save(order);

            orderSagaEvent.setMessage("Inventory check failed, cancelling order");
            log.error("Kiểm tra hàng tồn kho thất bại cho đơn hàng: {}", order.getId());
            kafkaTemplate.send("order-cancellation-topic", orderSagaEvent);
        }
    }

    @KafkaListener(topics = "payment-response-topic")
    @Transactional
    public void handlePaymentResponse(OrderSagaEvent orderSagaEvent)
    {
        log.info("Nhận phản hồi từ Payment Service: {}", orderSagaEvent);

        Order order = orderRepository.findById(orderSagaEvent.getOrder().getOrderId())
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        if(orderSagaEvent.getOrderStatus() == OrderStatus.PAYMENT_COMPLETED)
        {
            order.setOrderStatus(OrderStatus.INVENTORY_PROCESSING);
            orderRepository.save(order);

            orderSagaEvent.setOrderStatus(OrderStatus.INVENTORY_PROCESSING);
            orderSagaEvent.setMessage("Payment completed, processing inventory");
            kafkaTemplate.send("inventory-processing-topic", orderSagaEvent);
        } else if (orderSagaEvent.getOrderStatus() == OrderStatus.PAYMENT_FAILED) {

            order.setOrderStatus(OrderStatus.ORDER_CANCELLED);
            orderRepository.save(order);

            orderSagaEvent.setMessage("Payment failed, cancelling order");
            log.error("Thanh toán thất bại cho đơn hàng: {}", order.getId());
            kafkaTemplate.send("order-cancellation-topic", orderSagaEvent);
        }
    }

    @KafkaListener(topics = "payment-refundResponse-topic")
    @Transactional
    public void handlePaymentRefundResponse(OrderSagaEvent orderSagaEvent)
    {
        log.info("Nhận phản hồi từ Payment Service: {}", orderSagaEvent);

        Order order = orderRepository.findById(orderSagaEvent.getOrder().getOrderId())
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        if(orderSagaEvent.getOrderStatus() == OrderStatus.PAYMENT_REFUND_COMPLETED)
        {
            order.setOrderStatus(OrderStatus.ORDER_CANCELLED);
            orderRepository.save(order);

            orderSagaEvent.setMessage("Payment refund completed, cancelling order");
            log.error("Hoàn tiền thất bại cho đơn hàng: {}", order.getId());
            kafkaTemplate.send("order-cancellation-topic", orderSagaEvent);
        }else if (orderSagaEvent.getOrderStatus() == OrderStatus.PAYMENT_REFUND_FAILED) {
            order.setOrderStatus(OrderStatus.ORDER_CANCELLED);
            orderRepository.save(order);

            orderSagaEvent.setMessage("Payment refund failed, cancelling order");
            log.error("Hoàn tiền thất bại cho đơn hàng: {}", order.getId());
            kafkaTemplate.send("order-cancellation-topic", orderSagaEvent);
        }
    }

    @KafkaListener(topics = "inventory-response-topic")
    @Transactional
    public void handleInventoryResponse(OrderSagaEvent orderSagaEvent)
    {
        log.info("Nhận phản hồi từ Inventory Service: {}", orderSagaEvent);

        Order order = orderRepository.findById(orderSagaEvent.getOrder().getOrderId())
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        if(orderSagaEvent.getOrderStatus() == OrderStatus.INVENTORY_COMPLETED)
        {
            order.setOrderStatus(OrderStatus.DELIVERY_PROCESSING);
            orderRepository.save(order);

            orderSagaEvent.setOrderStatus(OrderStatus.DELIVERY_PROCESSING);
            orderSagaEvent.setMessage("Inventory processing completed");
            kafkaTemplate.send("delivery-processing-topic", orderSagaEvent);
        } else if (orderSagaEvent.getOrderStatus() == OrderStatus.INVENTORY_FAILED) {

            order.setOrderStatus(OrderStatus.ORDER_CANCELLED);
            orderRepository.save(order);

            orderSagaEvent.setOrderStatus(OrderStatus.ORDER_CANCELLED);
            orderSagaEvent.setMessage("Inventory processing failed, cancelling order");
            log.error("Xử lý hàng tồn kho thất bại cho đơn hàng: {}", order.getId());
            kafkaTemplate.send("payment-refund-topic", orderSagaEvent);
        }
    }

    @KafkaListener(topics = "delivery-response-topic")
    @Transactional
    public void handleDeliveryResponse(OrderSagaEvent orderSagaEvent)
    {
        log.info("Nhận phản hồi từ Delivery Service: {}", orderSagaEvent);

        Order order = orderRepository.findById(orderSagaEvent.getOrder().getOrderId())
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        if(orderSagaEvent.getOrderStatus() == OrderStatus.DELIVERY_COMPLETED)
        {
            order.setOrderStatus(OrderStatus.ORDER_COMPLETED);
            orderRepository.save(order);

            orderSagaEvent.setMessage("Delivery completed");
            kafkaTemplate.send("order-completed-topic", orderSagaEvent);
        } else if (orderSagaEvent.getOrderStatus() == OrderStatus.DELIVERY_FAILED) {

            order.setOrderStatus(OrderStatus.ORDER_CANCELLED);
            orderRepository.save(order);

            orderSagaEvent.setMessage("Delivery failed, cancelling order");
            log.error("Giao hàng thất bại cho đơn hàng: {}", order.getId());
            kafkaTemplate.send("payment-refund-topic", orderSagaEvent);
            kafkaTemplate.send("inventory-revert-topic", orderSagaEvent);
        }
    }
}
