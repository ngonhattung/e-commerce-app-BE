package com.nhattung.userservice.exception;

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
    EMAIL_EXISTED(1008, "Email existed, please choose another one", HttpStatus.BAD_REQUEST),
    USER_EXISTED(1009, "Username existed, please choose another one", HttpStatus.BAD_REQUEST),
    USERNAME_IS_MISSING(1010, "Please enter username", HttpStatus.BAD_REQUEST),
    USER_NOT_EXISTED(1011, "User not existed", HttpStatus.BAD_REQUEST),
    EMPTY_STREET(1012, "Street cannot be empty", HttpStatus.BAD_REQUEST),
    INVALID_STREET(1013, "Street name must be between 3 and 100 characters", HttpStatus.BAD_REQUEST),
    EMPTY_CITY(1014, "City cannot be empty", HttpStatus.BAD_REQUEST),
    INVALID_CITY(1015, "City name must be between 2 and 50 characters", HttpStatus.BAD_REQUEST),
    EMPTY_DISTRICT(1016, "District cannot be empty", HttpStatus.BAD_REQUEST),
    INVALID_DISTRICT(1017, "District name must be between 2 and 50 characters", HttpStatus.BAD_REQUEST),
    EMPTY_FULLNAME(1018, "Fullname cannot be empty", HttpStatus.BAD_REQUEST),
    EMPTY_EMAIL(1033, "Email is required", HttpStatus.BAD_REQUEST),
    EMPTY_PASSWORD(1044, "Password is required", HttpStatus.BAD_REQUEST),
    EMPTY_PHONE(1019, "Phone number cannot be empty", HttpStatus.BAD_REQUEST),
    INVALID_PHONE(1020, "Phone number must be between 10 and 15 characters", HttpStatus.BAD_REQUEST),
    EMPTY_AVATAR(1021, "Avatar cannot be empty", HttpStatus.BAD_REQUEST),
    INVALID_AVATAR(1022, "Avatar must be a valid URL", HttpStatus.BAD_REQUEST),
    INVALID_BIRTHDAY(1023, "Birthday must be in the past", HttpStatus.BAD_REQUEST),
    INVALID_PAGE_SIZE(1024, "Page size must be between 1 and 100", HttpStatus.BAD_REQUEST),
    FILE_SIZE_TOO_LARGE(1025, "File size too large", HttpStatus.BAD_REQUEST),
    INVALID_FILE_TYPE(1026, "Invalid file type", HttpStatus.BAD_REQUEST),
    UPLOAD_IMAGE_ERROR(1027, "Upload image error", HttpStatus.INTERNAL_SERVER_ERROR),
    OLD_PASSWORD_NOT_MATCH(1028, "Old password not match", HttpStatus.BAD_REQUEST),

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
