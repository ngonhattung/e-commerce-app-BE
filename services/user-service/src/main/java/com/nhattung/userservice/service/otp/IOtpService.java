package com.nhattung.userservice.service.otp;


import com.nhattung.userservice.request.GenerateOtpRequest;
import com.nhattung.userservice.request.ValidateOtpRequest;
import com.nhattung.userservice.response.OtpResponse;
import com.nhattung.userservice.response.ValidateOtpResponse;

public interface IOtpService {

    OtpResponse generateOtp(GenerateOtpRequest request);
    ValidateOtpResponse validateOtp(ValidateOtpRequest request);
}
