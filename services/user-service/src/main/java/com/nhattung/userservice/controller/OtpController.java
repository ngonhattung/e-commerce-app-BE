package com.nhattung.userservice.controller;

import com.nhattung.userservice.request.GenerateOtpRequest;
import com.nhattung.userservice.request.ValidateOtpRequest;
import com.nhattung.userservice.response.ApiResponse;
import com.nhattung.userservice.response.OtpResponse;
import com.nhattung.userservice.response.ValidateOtpResponse;
import com.nhattung.userservice.service.otp.IOtpService;
import com.nhattung.userservice.service.otp.OtpService;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.shaded.com.google.protobuf.Api;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/otp")
public class OtpController {

    private final IOtpService otpService;

    @PostMapping("/generate")
    public ApiResponse<OtpResponse> generateOtp(@RequestBody GenerateOtpRequest request) {
        return ApiResponse.<OtpResponse>builder()
                .message("OTP generated successfully")
                .result(otpService.generateOtp(request))
                .build();
    }

    @PostMapping("/validate")
    public ApiResponse<ValidateOtpResponse> validateOtp(@RequestBody ValidateOtpRequest request)
    {
        return ApiResponse.<ValidateOtpResponse>builder()
                .result(otpService.validateOtp(request))
                .build();
    }

}
