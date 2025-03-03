package com.nhattung.userservice.exception;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String mess) {
        super(mess);
    }
}
