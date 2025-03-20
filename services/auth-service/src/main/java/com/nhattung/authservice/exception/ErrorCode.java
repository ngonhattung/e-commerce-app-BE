package com.nhattung.authservice.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;


@Getter
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_KEY(1001, "Uncategorized error", HttpStatus.BAD_REQUEST),
    INVALID_EMAIL(1003, "Email invalid", HttpStatus.BAD_REQUEST),
    INVALID_PASSWORD(1004, "Password must be at least 8 characters", HttpStatus.BAD_REQUEST),
    UNAUTHENTICATED(1006, "Unauthenticated", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1007, "You do not have permission", HttpStatus.FORBIDDEN),
    INVALID_CREDENTIALS(1012, "Username or password not correct", HttpStatus.UNAUTHORIZED),
    FEIGN_ERROR(1013, "Feign error", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_REFRESH_TOKEN(1014, "Invalid refresh token", HttpStatus.BAD_REQUEST),
    ERROR_REFRESH_TOKEN(1015, "Token invalid or expired", HttpStatus.BAD_REQUEST),
    EMPTY_EMAIL(1033, "Email is required", HttpStatus.BAD_REQUEST),
    EMPTY_PASSWORD(1044, "Password is required", HttpStatus.BAD_REQUEST),
            ;
    ErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }
    private final int code;
    private final HttpStatusCode statusCode;
    private final String message;

}
