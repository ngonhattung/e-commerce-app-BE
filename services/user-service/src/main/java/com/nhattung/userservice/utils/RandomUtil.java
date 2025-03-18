package com.nhattung.userservice.utils;

import org.springframework.stereotype.Component;

import java.util.UUID;
@Component
public class RandomUtil {

    public String generateRandomString() {
        return UUID.randomUUID().toString().replaceAll("-", "").substring(0, 10);
    }
}
