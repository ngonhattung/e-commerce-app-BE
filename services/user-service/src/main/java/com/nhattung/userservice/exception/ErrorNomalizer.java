package com.nhattung.userservice.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhattung.userservice.response.KeycloakError;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Configuration
@Slf4j
public class ErrorNomalizer {

    private final ObjectMapper objectMapper;
    private final Map<String,ErrorCode> errorCodeMap;
    public ErrorNomalizer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        errorCodeMap = new HashMap<>();
        errorCodeMap.put("User exists with same email", ErrorCode.EMAIL_EXISTED);
        errorCodeMap.put("User exists with same username", ErrorCode.USER_EXISTED);
    }


    public AppException handelKeyCloakException(RuntimeException exception)
    {
        try {
            log.warn("Cannot complete the request: {}", exception.getMessage());
            var response = objectMapper.readValue(exception.getMessage(), KeycloakError.class);

            if(Objects.nonNull(response.getErrorMessage()) &&
                    Objects.nonNull(errorCodeMap.get(response.getErrorMessage()))){
                return new AppException(errorCodeMap.get(response.getErrorMessage()));
            }
        } catch (JsonProcessingException e) {
            log.error("ErrorNomalizer.handelKeyCloakException: ", e);
        }

        return new AppException((ErrorCode.UNCATEGORIZED_EXCEPTION));
    }
}
