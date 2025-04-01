package com.nhattung.userservice.service.otp;


import com.nhattung.event.dto.NotificationEvent;
import com.nhattung.userservice.request.GenerateOtpRequest;
import com.nhattung.userservice.request.ValidateOtpRequest;
import com.nhattung.userservice.response.OtpResponse;
import com.nhattung.userservice.response.ValidateOtpResponse;
import com.nhattung.userservice.service.redis.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class OtpService implements IOtpService{

    private final RedisService redisService;
    private static final String OTP_PREFIX = "OTP_";
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public OtpResponse generateOtp(GenerateOtpRequest request) {
        String otp = String.valueOf(100000 + new Random().nextInt(900000));
        String key = OTP_PREFIX + request.getEmail();
        redisService.saveOTP(key, otp, 2);

        NotificationEvent notificationEvent = NotificationEvent.builder()
                .channel("email")
                .receiver(request.getEmail())
                .templateCode("SEND_OTP")
                .params(Map.of(
                        "subject","Your OTP Code for Verification From DreamShop",
                        "content", formSendOtp(otp)
                ))
                .build();
        kafkaTemplate.send("notification-delivery", notificationEvent);
        return OtpResponse.builder()
                .otp(otp)
                .build();
    }

    @Override
    public ValidateOtpResponse validateOtp(ValidateOtpRequest request) {
        String storedOtp = redisService.getOTP(OTP_PREFIX + request.getEmail());
        if(storedOtp != null && storedOtp.equals(request.getOtp())){
            redisService.deleteOTP(OTP_PREFIX + request.getEmail());
            return ValidateOtpResponse.builder()
                    .message("OTP is valid")
                    .isValid(true)
                    .build();
        }
        return ValidateOtpResponse.builder()
                .isValid(false)
                .message("OTP invalid or expired")
                .build();
    }

    private String formSendOtp (String otp)
    {
        return "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "    <title>OTP Verification</title>\n" +
                "    <style>\n" +
                "        body {\n" +
                "            font-family: Arial, sans-serif;\n" +
                "            background-color: #f4f4f4;\n" +
                "            margin: 0;\n" +
                "            padding: 0;\n" +
                "        }\n" +
                "        .container {\n" +
                "            max-width: 600px;\n" +
                "            margin: 20px auto;\n" +
                "            background: #ffffff;\n" +
                "            padding: 20px;\n" +
                "            border-radius: 10px;\n" +
                "            box-shadow: 0px 0px 10px rgba(0, 0, 0, 0.1);\n" +
                "            text-align: center;\n" +
                "        }\n" +
                "        .header {\n" +
                "            display: flex;\n" +
                "            justify-content: center;\n" +
                "            align-items: center;\n" +
                "            padding: 10px 0;\n" +
                "            width: 100%;\n" +
                "        }\n" +
                "        .header img {\n" +
                "            max-width: 300px;\n" +
                "            height: auto;\n" +
                "            display: block;\n" +
                "            margin: 0 auto;\n" +
                "        }\n" +
                "        .content {\n" +
                "            color: #333;\n" +
                "        }\n" +
                "        .otp {\n" +
                "            font-size: 24px;\n" +
                "            font-weight: bold;\n" +
                "            color: #ff6600;\n" +
                "            margin: 20px 0;\n" +
                "        }\n" +
                "        .footer {\n" +
                "            font-size: 12px;\n" +
                "            color: #777;\n" +
                "            margin-top: 20px;\n" +
                "        }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <div class=\"container\">\n" +
                "        <div class=\"header\">\n" +
                "            <img src=\"https://res.cloudinary.com/dclf0ngcu/image/upload/v1743265120/dreamy-mart/logo-blue_cnfw0g.png\" alt=\"Logo\">\n" +
                "        </div>\n" +
                "        <div class=\"content\">\n" +
                "            <h2>OTP Verification</h2>\n" +
                "            <p>Your One-Time Password (OTP) for verification is:</p>\n" +
                "            <div class=\"otp\">" + otp + "</div>\n" +
                "            <p>Please enter this OTP to complete your verification. This OTP is valid for 1 minute.</p>\n" +
                "        </div>\n" +
                "        <div class=\"footer\">\n" +
                "            <p>If you did not request this, please ignore this email.</p>\n" +
                "            <p>&copy; 2025 Your E-Commerce Store. All rights reserved.</p>\n" +
                "        </div>\n" +
                "    </div>\n" +
                "</body>\n" +
                "</html>";
    }
}
