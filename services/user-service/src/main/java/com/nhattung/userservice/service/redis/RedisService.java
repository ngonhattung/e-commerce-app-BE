package com.nhattung.userservice.service.redis;


import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RedisService implements IRedisService{

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public void saveOTP(String key, String otp, long timeout) {
            redisTemplate.opsForValue().set(key, otp, timeout, TimeUnit.MINUTES);
    }

    @Override
    public String getOTP(String key) {
        return (String) redisTemplate.opsForValue().get(key);
    }

    @Override
    public void deleteOTP(String key) {
        redisTemplate.delete(key);
    }
}
