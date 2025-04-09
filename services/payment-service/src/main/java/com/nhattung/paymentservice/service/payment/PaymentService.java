package com.nhattung.paymentservice.service.payment;

import com.nhattung.paymentservice.entity.Payment;
import com.nhattung.paymentservice.exception.AppException;
import com.nhattung.paymentservice.exception.ErrorCode;
import com.nhattung.paymentservice.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentService implements IPaymentService{

    private final PaymentRepository paymentRepository;

    @Override
    public void savePayment(Payment payment) {
        paymentRepository.save(payment);
    }

    @Override
    public Payment getPaymentById(Long id) {
        return paymentRepository.findById(id).orElseThrow(
                () -> new AppException(ErrorCode.PAYMENT_NOT_FOUND));
    }

    @Override
    public Payment getPaymentByOrderId(String orderId) {
        return paymentRepository.findByOrderId(orderId).orElseThrow(
                () -> new AppException(ErrorCode.PAYMENT_NOT_FOUND));
    }

    @Override
    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }
}
