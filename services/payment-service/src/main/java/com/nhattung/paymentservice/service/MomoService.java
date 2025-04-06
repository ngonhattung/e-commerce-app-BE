package com.nhattung.paymentservice.service;

import com.nhattung.paymentservice.exception.AppException;
import com.nhattung.paymentservice.exception.ErrorCode;
import com.nhattung.paymentservice.repository.httpclient.MomoClient;
import com.nhattung.paymentservice.request.MoMoPaymentRequest;
import com.nhattung.paymentservice.response.MoMoPaymentResponse;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.binary.Hex;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Service
@RequiredArgsConstructor
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

    public MoMoPaymentResponse createPayment() {

        String orderId = UUID.randomUUID().toString();
        String requestId = UUID.randomUUID().toString();
        Long amount = 10000L; // Amount in VND
        String orderInfo = "Payment for order " + orderId;
        String extraData = "Khong co khuyen mai"; // Optional extra data
        String lang = "vi"; // Language for the payment page



        String rawSignature = String.format(
                "accessKey=%s&amount=%d&extraData=%s&ipnUrl=%s&orderId=%s&orderInfo=%s&partnerCode=%s&redirectUrl=%s&requestId=%s&requestType=%s",
                ACCESS_KEY, amount, extraData, IPN_URL, orderId, orderInfo, PARTNER_CODE, REDIRECT_URL, requestId, REQUEST_TYPE);

        String signature = "";
        try {
            signature = signSHA256(rawSignature, SECRET_KEY);
        } catch (Exception e) {
            throw new AppException(ErrorCode.ERROR_HASH);
        }
        if(signature.isEmpty()) {
            throw new AppException(ErrorCode.ERROR_HASH);
        }
        MoMoPaymentRequest momoRequest = MoMoPaymentRequest.builder()
                .partnerCode(PARTNER_CODE)
                .requestType(REQUEST_TYPE)
                .ipnUrl(IPN_URL)
                .redirectUrl(REDIRECT_URL)
                .orderId(orderId)
                .amount(amount)
                .orderInfo(orderInfo)
                .requestId(requestId)
                .lang(lang)
                .extraData(extraData)
                .signature(signature)
                .build();

        return momoClient.createPayment(momoRequest);
    }


    private static String signSHA256(String rawData, String secretKey) throws Exception {
        Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec secret_key = new SecretKeySpec(secretKey.getBytes(), "HmacSHA256");
        sha256_HMAC.init(secret_key);
        byte[] hash = sha256_HMAC.doFinal(rawData.getBytes(StandardCharsets.UTF_8));
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xFF & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString().toUpperCase();
    }


}
