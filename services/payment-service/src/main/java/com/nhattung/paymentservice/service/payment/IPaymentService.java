package com.nhattung.paymentservice.service.payment;

import com.nhattung.paymentservice.entity.Payment;

import java.util.List;

public interface IPaymentService {
    void savePayment(Payment payment);
    Payment getPaymentById(Long id);
    Payment getPaymentByOrderId(String orderId);
    List<Payment> getAllPayments();

}
