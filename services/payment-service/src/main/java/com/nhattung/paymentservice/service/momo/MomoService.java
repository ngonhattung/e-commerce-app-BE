package com.nhattung.paymentservice.service.momo;

import com.nhattung.dto.OrderSagaDto;
import com.nhattung.enums.OrderStatus;
import com.nhattung.event.dto.OrderSagaEvent;
import com.nhattung.paymentservice.entity.Payment;
import com.nhattung.paymentservice.exception.AppException;
import com.nhattung.paymentservice.exception.ErrorCode;
import com.nhattung.paymentservice.repository.httpclient.MomoClient;
import com.nhattung.paymentservice.request.MoMoCallbackRequest;
import com.nhattung.paymentservice.request.MoMoPaymentRequest;
import com.nhattung.paymentservice.request.MoMoRefundRequest;
import com.nhattung.paymentservice.request.PaymentRequest;
import com.nhattung.paymentservice.response.MoMoPaymentResponse;
import com.nhattung.paymentservice.service.payment.IPaymentService;
import com.nhattung.paymentservice.service.payment.PaymentService;
import com.nhattung.paymentservice.utils.MoMoSignatureUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MomoService {

    @Value("${momo.partner-code}")
    private String PARTNER_CODE;
    @Value("${momo.access-key}")
    private String ACCESS_KEY;
    @Value("${momo.secret-key}")
    private String SECRET_KEY;
    @Value("${momo.redirect-url}")
    private String REDIRECT_URL;
    @Value("${momo.ipn-url}")
    private String IPN_URL;
    @Value("${momo.request-type}")
    private String REQUEST_TYPE;

    private final MomoClient momoClient;
    private final MoMoSignatureUtil moMoSignatureUtil;
    private final IPaymentService paymentService;
    private final KafkaTemplate<String, OrderSagaEvent> kafkaTemplate;
    public MoMoPaymentResponse createPayment(PaymentRequest request) {

        String orderId = request.getOrderId();
        String requestId = UUID.randomUUID().toString();
        BigDecimal amount = request.getTotalAmount();


        String orderInfo = "Payment for order " + orderId;
        String extraData = ""; // Optional extra data
        String lang = "vi"; // Language for the payment page
        String rawSignature = String.format(
                "accessKey=%s&amount=%s&extraData=%s&ipnUrl=%s&orderId=%s&orderInfo=%s&partnerCode=%s&redirectUrl=%s&requestId=%s&requestType=%s",
                ACCESS_KEY, amount.longValue(), extraData, IPN_URL, orderId, orderInfo, PARTNER_CODE, REDIRECT_URL, requestId, REQUEST_TYPE);

        log.info("Raw signature: " + rawSignature);
        String signature = "";
        try {
            signature = moMoSignatureUtil.signSHA256(rawSignature, SECRET_KEY);
        } catch (Exception e) {
            throw new AppException(ErrorCode.ERROR_HASH);
        }
        if (signature.isEmpty()) {
            throw new AppException(ErrorCode.ERROR_HASH);
        }
        MoMoPaymentRequest momoRequest = MoMoPaymentRequest.builder()
                .partnerCode(PARTNER_CODE)
                .requestType(REQUEST_TYPE)
                .ipnUrl(IPN_URL)
                .redirectUrl(REDIRECT_URL)
                .orderId(orderId)
                .amount(amount.longValue())
                .orderInfo(orderInfo)
                .requestId(requestId)
                .lang(lang)
                .extraData(extraData)
                .signature(signature)
                .build();

        return momoClient.createPayment(momoRequest);
    }

    public MoMoPaymentResponse refundPayment(PaymentRequest request) {
        String orderId = request.getOrderId();
        String requestId = UUID.randomUUID().toString();
        BigDecimal amount = request.getTotalAmount();
        String lang = "vi";

        String rawSignature = String.format("accessKey=%s&amount=%s&orderId=%s&partnerCode=%s&requestId=%s",
                ACCESS_KEY, amount.longValue(), orderId, PARTNER_CODE, requestId);

        log.info("Raw signature: " + rawSignature);

        String signature = "";
        try {
            signature = moMoSignatureUtil.signSHA256(rawSignature, SECRET_KEY);
        } catch (Exception e) {
            throw new AppException(ErrorCode.ERROR_HASH);
        }
        if (signature.isEmpty()) {
            throw new AppException(ErrorCode.ERROR_HASH);
        }
        MoMoRefundRequest refundRequest = MoMoRefundRequest.builder()
                .partnerCode(PARTNER_CODE)
                .accessKey(ACCESS_KEY)
                .requestId(requestId)
                .orderId(orderId)
                .amount(amount.longValue())
                .signature(signature)
                .lang(lang)
                .build();

        return momoClient.refundPayment(refundRequest);
    }

    public void processPaymentResponse(MoMoCallbackRequest request) {
        String newAccessKey = request.getAccessKey();
        if (request.getAccessKey() == null || request.getAccessKey().isEmpty()) {
            newAccessKey = ACCESS_KEY;
        }
        try {
            String rawSignature = "accessKey=" + newAccessKey
                    + "&amount=" + request.getAmount()
                    + "&extraData=" + request.getExtraData()
                    + "&message=" + request.getMessage()
                    + "&orderId=" + request.getOrderId()
                    + "&orderInfo=" + request.getOrderInfo()
                    + "&orderType=" + request.getOrderType()
                    + "&partnerCode=" + request.getPartnerCode()
                    + "&payType=" + request.getPayType()
                    + "&requestId=" + request.getRequestId()
                    + "&responseTime=" + request.getResponseTime()
                    + "&resultCode=" + request.getResultCode()
                    + "&transId=" + request.getTransId();
            String signature = "";
            try {
                signature = moMoSignatureUtil.signSHA256(rawSignature, SECRET_KEY);
            } catch (Exception e) {
                throw new AppException(ErrorCode.ERROR_HASH);
            }
            if (signature.isEmpty()) {
                throw new AppException(ErrorCode.ERROR_HASH);
            }

            OrderSagaEvent paymentSagaEvent = new OrderSagaEvent();
            paymentSagaEvent.setOrder(new OrderSagaDto(request.getOrderId()));

            Payment payment = paymentService.getPaymentByOrderId(request.getOrderId());
            if (request.getResultCode() == 0 && request.getSignature().equals(signature)) {
                log.info("Payment successful: {}", request);
                payment.setPaymentStatus(OrderStatus.PAYMENT_COMPLETED);
                paymentSagaEvent.setOrderStatus(OrderStatus.PAYMENT_COMPLETED);
                paymentSagaEvent.setMessage("Payment completed successfully");
                paymentService.savePayment(payment);
            } else {
                log.info("Payment failed: {}", request);
                payment.setPaymentStatus(OrderStatus.PAYMENT_FAILED);
                paymentSagaEvent.setOrderStatus(OrderStatus.PAYMENT_FAILED);
                paymentSagaEvent.setMessage("Payment failed");
                paymentService.savePayment(payment);
            }

            // Send payment saga event to Kafka
            kafkaTemplate.send("payment-response-topic", paymentSagaEvent);


        } catch (Exception e) {
            log.error("Error processing payment response: {}", e.getMessage());
            throw new AppException(ErrorCode.ERROR_HASH);

        }

    }


}
