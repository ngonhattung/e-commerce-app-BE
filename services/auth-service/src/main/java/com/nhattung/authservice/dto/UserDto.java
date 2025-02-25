package com.nhattung.authservice.dto;

import lombok.Data;

@Data
public class UserDto {
    private Long id;
    private String email;
    private String password;
}
