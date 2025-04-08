package com.nhattung.paymentservice.service.payment;

import com.nhattung.paymentservice.entity.Payment;

import java.util.List;

public interface IPaymentService {
    Payment savePayment(Payment payment);
    Payment getPaymentById(Long id);
    Payment getPaymentByOrderId(Long orderId);
    List<Payment> getAllPayments();

}
