package com.nhattung.orderservice.controller.saga;

import com.nhattung.dto.OrderItemSagaDto;
import com.nhattung.enums.OrderStatus;
import com.nhattung.event.dto.NotificationEvent;
import com.nhattung.event.dto.OrderSagaEvent;
import com.nhattung.orderservice.dto.OrderDto;
import com.nhattung.orderservice.dto.OrderItemDto;
import com.nhattung.orderservice.entity.Order;
import com.nhattung.orderservice.entity.OrderItem;
import com.nhattung.orderservice.enums.CancelReason;
import com.nhattung.orderservice.exception.AppException;
import com.nhattung.orderservice.exception.ErrorCode;
import com.nhattung.orderservice.repository.OrderRepository;
import com.nhattung.orderservice.repository.httpclient.CartClient;
import com.nhattung.orderservice.service.IOrderService;
import com.nhattung.orderservice.utils.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderSaga {

    private final OrderRepository orderRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final AuthenticatedUser authenticatedUser;
    @KafkaListener(topics = "inventory-checkingResponse-topic")
    @Transactional
    public void handleInventoryChecked(OrderSagaEvent orderSagaEvent) {
        log.info("Nhận phản hồi từ Inventory Service: {}", orderSagaEvent);
        Order order = orderRepository.findById(orderSagaEvent.getOrder().getOrderId())
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        if (orderSagaEvent.getOrderStatus() == OrderStatus.INVENTORY_CHECKED) {
            order.setOrderStatus(OrderStatus.PAYMENT_PROCESSING);
            orderRepository.save(order);
            orderSagaEvent.setOrderStatus(OrderStatus.PAYMENT_PROCESSING);
            orderSagaEvent.setMessage("Inventory checked, processing payment");
            kafkaTemplate.send("payment-processing-topic", orderSagaEvent);
        } else if (orderSagaEvent.getOrderStatus() == OrderStatus.INVENTORY_FAILED) {

            order.setOrderStatus(OrderStatus.ORDER_CANCELLED);
            order.setCancelReason(CancelReason.INVENTORY_FAILED_AVAILABLE);
            orderRepository.save(order);

            orderSagaEvent.setMessage("Inventory check failed, cancelling order");
//            log.error("Kiểm tra hàng tồn kho thất bại cho đơn hàng: {}", order.getId());
//            sendMailOrder("Order cancelled",
//                    String.valueOf(CancelReason.INVENTORY_FAILED_AVAILABLE),101, orderSagaEvent);
        }
    }



    @KafkaListener(topics = "payment-response-topic")
    @Transactional
    public void handlePaymentResponse(OrderSagaEvent orderSagaEvent) {
        log.info("Nhận phản hồi từ Payment Service: {}", orderSagaEvent);

        Order order = orderRepository.findById(orderSagaEvent.getOrder().getOrderId())
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        if (orderSagaEvent.getOrderStatus() == OrderStatus.PAYMENT_COMPLETED) {
            order.setOrderStatus(OrderStatus.INVENTORY_PROCESSING);
            order.setTransId(orderSagaEvent.getOrder().getTransId());
            orderRepository.save(order);

            orderSagaEvent.setOrderStatus(OrderStatus.INVENTORY_PROCESSING);
            orderSagaEvent.setMessage("Payment completed, processing inventory");
            kafkaTemplate.send("inventory-processing-topic", orderSagaEvent);
        } else if (orderSagaEvent.getOrderStatus() == OrderStatus.PAYMENT_FAILED) {

            order.setOrderStatus(OrderStatus.ORDER_CANCELLED);
            order.setCancelReason(CancelReason.PAYMENT_FAILED);
            orderRepository.save(order);

            orderSagaEvent.setMessage("Payment failed, cancelling order");
            log.error("Thanh toán thất bại cho đơn hàng: {}", order.getId());
            sendMailOrder("Order cancelled",
                    String.valueOf(CancelReason.PAYMENT_FAILED), 102, orderSagaEvent);
        }
    }

    @KafkaListener(topics = "payment-refundResponse-topic")
    @Transactional
    public void handlePaymentRefundResponse(OrderSagaEvent orderSagaEvent) {
        log.info("Nhận phản hồi từ Payment Service: {}", orderSagaEvent);

        Order order = orderRepository.findById(orderSagaEvent.getOrder().getOrderId())
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        if (orderSagaEvent.getOrderStatus() == OrderStatus.PAYMENT_REFUND_COMPLETED) {
            order.setOrderStatus(OrderStatus.ORDER_CANCELLED);
            orderRepository.save(order);

            orderSagaEvent.setMessage("Payment refund completed, cancelling order");
            log.error("Hoàn tiền thất bại cho đơn hàng: {}", order.getId());
        } else if (orderSagaEvent.getOrderStatus() == OrderStatus.PAYMENT_REFUND_FAILED) {
            order.setOrderStatus(OrderStatus.ORDER_CANCELLED);
            orderRepository.save(order);

            orderSagaEvent.setMessage("Payment refund failed, cancelling order");
            log.error("Hoàn tiền thất bại cho đơn hàng: {}", order.getId());
        }
       // kafkaTemplate.send("refund-response-topic", orderSagaEvent); //send email
        sendMailOrder("Payment refund"
                , orderSagaEvent.getMessage(), 103, orderSagaEvent);
    }

    @KafkaListener(topics = "inventory-response-topic")
    @Transactional
    public void handleInventoryResponse(OrderSagaEvent orderSagaEvent) {
        log.info("Nhận phản hồi từ Inventory Service: {}", orderSagaEvent);

        Order order = orderRepository.findById(orderSagaEvent.getOrder().getOrderId())
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        if (orderSagaEvent.getOrderStatus() == OrderStatus.INVENTORY_COMPLETED) {
            order.setOrderStatus(OrderStatus.DELIVERY_PROCESSING);
            orderRepository.save(order);

            orderSagaEvent.setOrderStatus(OrderStatus.DELIVERY_PROCESSING);
            orderSagaEvent.setMessage("Inventory processing completed");
            kafkaTemplate.send("delivery-processing-topic", orderSagaEvent);
        } else if (orderSagaEvent.getOrderStatus() == OrderStatus.INVENTORY_FAILED) {

            order.setOrderStatus(OrderStatus.ORDER_CANCELLED);
            order.setCancelReason(CancelReason.INVENTORY_FAILED_AFTER_PAYMENT);
            orderRepository.save(order);

            orderSagaEvent.setOrderStatus(OrderStatus.ORDER_CANCELLED);
            orderSagaEvent.setMessage("Inventory processing failed, cancelling order");
            log.error("Xử lý hàng tồn kho thất bại cho đơn hàng: {}", order.getId());
            sendMailOrder("Order cancelled",
                    String.valueOf(CancelReason.INVENTORY_FAILED_AFTER_PAYMENT), 104, orderSagaEvent);
            kafkaTemplate.send("payment-refund-topic", orderSagaEvent);
        }
    }

    @KafkaListener(topics = "delivery-response-topic")
    @Transactional
    public void handleDeliveryResponse(OrderSagaEvent orderSagaEvent) {
        log.info("Nhận phản hồi từ Delivery Service: {}", orderSagaEvent);

        Order order = orderRepository.findById(orderSagaEvent.getOrder().getOrderId())
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        if (orderSagaEvent.getOrderStatus() == OrderStatus.DELIVERY_COMPLETED) {
            order.setOrderStatus(OrderStatus.ORDER_COMPLETED);
            order.setCancelReason(null);
            orderRepository.save(order);

            orderSagaEvent.setMessage("Delivery completed");
            sendMailOrder("Order completed", "Delivery completed", 999, orderSagaEvent);
        } else if (orderSagaEvent.getOrderStatus() == OrderStatus.DELIVERY_FAILED) {

            order.setOrderStatus(OrderStatus.ORDER_CANCELLED);
            order.setCancelReason(CancelReason.DELIVERY_FAILED);
            orderRepository.save(order);

            orderSagaEvent.setMessage("Delivery failed, cancelling order");
            log.error("Giao hàng thất bại cho đơn hàng: {}", order.getId());
            kafkaTemplate.send("payment-refund-topic", orderSagaEvent);
            kafkaTemplate.send("inventory-revert-topic", orderSagaEvent);
            sendMailOrder("Order cancelled",
                    String.valueOf(CancelReason.DELIVERY_FAILED), 105, orderSagaEvent);
        }
    }

    public void sendMailOrder(String subject, String reason, int formCode, OrderSagaEvent orderSagaEvent) {
        NotificationEvent notificationEvent = NotificationEvent.builder()
                .channel("email")
                .receiver(orderSagaEvent.getOrder().getEmail())
                .templateCode("ORDER_EMAIL")
                .params(Map.of(
                        "subject", subject,
                        "content", formCancelOrderEmailContent(reason, formCode, orderSagaEvent)
                ))
                .build();
        kafkaTemplate.send("notification-delivery", notificationEvent);
    }

    public String formCancelOrderEmailContent(String reason, int formCode, OrderSagaEvent orderSagaEvent) {
        StringBuilder html = new StringBuilder();

        html.append("""
        <!DOCTYPE html>
        <html lang="vi">
        <head>
            <meta charset="UTF-8">
            <style>
                body {
                    font-family: Arial, sans-serif;
                    background-color: #f2f4f6;
                    margin: 0;
                    padding: 20px;
                    color: #333;
                }
                .container {
                    background-color: #ffffff;
                    border-radius: 8px;
                    padding: 20px;
                    max-width: 700px;
                    margin: auto;
                    box-shadow: 0 2px 8px rgba(0,0,0,0.1);
                }
                .logo {
                    text-align: center;
                    margin-bottom: 20px;
                }
                .logo img {
                    width: 150px;
                }
                h2 {
                    color: #007BFF;
                    text-align: center;
                }
                .status-message {
                    background-color: #fff3cd;
                    border: 1px solid #ffeeba;
                    padding: 15px;
                    border-radius: 5px;
                    margin-bottom: 20px;
                    font-size: 16px;
                }
                table {
                    width: 100%;
                    border-collapse: collapse;
                    margin-top: 20px;
                }
                th, td {
                    border: 1px solid #ddd;
                    padding: 10px;
                    text-align: center;
                }
                th {
                    background-color: #007BFF;
                    color: white;
                }
                .total {
                    text-align: right;
                    font-weight: bold;
                    font-size: 16px;
                    margin-top: 20px;
                }
            </style>
        </head>
        <body>
            <div class="container">
                <div class="logo">
                    <img src="https://res.cloudinary.com/dclf0ngcu/image/upload/v1743265120/dreamy-mart/logo-blue_cnfw0g.png" alt="Dreamy Mart Logo">
                </div>
    """);

        html.append("<h2>").append(reason).append("</h2>");

        html.append("<div class='status-message'>");
        switch (formCode) {
            case 102 -> html.append("Đơn hàng của bạn không thể hoàn tất do <strong>thanh toán thất bại</strong>.");
            case 103 -> html.append("Đơn hàng của bạn đã được <strong>hoàn tiền</strong>.");
            case 104 -> html.append("Kho hiện tại <strong>không đủ số lượng</strong> để đáp ứng đơn hàng của bạn.");
            case 105 -> html.append("<strong>Giao hàng thất bại</strong>. Chúng tôi rất tiếc vì sự bất tiện này.");
            case 999 -> html.append("Đơn hàng của bạn đã được <strong>giao thành công</strong>. Cảm ơn bạn đã mua sắm cùng chúng tôi!");
            default -> html.append("Trạng thái đơn hàng không xác định.");
        }
        html.append("</div>");

        html.append("<p><strong>Mã đơn hàng:</strong> ").append(orderSagaEvent.getOrder().getOrderId()).append("</p>");

        html.append("""
        <table>
            <thead>
                <tr>
                    <th>Tên sản phẩm</th>
                    <th>Số lượng</th>
                    <th>Đơn giá</th>
                    <th>Thành tiền</th>
                </tr>
            </thead>
            <tbody>
    """);

        for (var item : orderSagaEvent.getOrder().getOrderItems()) {
            BigDecimal total = item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            html.append("<tr>");
            html.append("<td>").append(item.getProductName()).append("</td>");
            html.append("<td>").append(item.getQuantity()).append("</td>");
            html.append("<td>").append(formatCurrency(item.getPrice())).append("</td>");
            html.append("<td>").append(formatCurrency(total)).append("</td>");
            html.append("</tr>");
        }

        html.append("</tbody></table>");

        html.append("<p class='total'>Tổng cộng: ").append(formatCurrency(orderSagaEvent.getOrder().getTotalPrice())).append("</p>");

        html.append("""
            </div>
        </body>
        </html>
    """);

        return html.toString();
    }


    private String formatCurrency(BigDecimal amount) {
        return String.format("%,.0f VND", amount);
    }
}
