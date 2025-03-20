package com.nhattung.userservice.service.redis;


public interface IRedisService {
    void saveOTP(String key, String otp, long timeout);
    String getOTP(String key);
    void deleteOTP(String key);
}
