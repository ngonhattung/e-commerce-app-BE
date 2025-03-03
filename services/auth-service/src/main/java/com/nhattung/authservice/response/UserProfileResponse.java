package com.nhattung.authservice.response;

import lombok.Data;

import java.time.LocalDate;

@Data
public class UserProfileResponse {
    private Long id;
    private String fullName;
    private String phone;
    private String avatar;
    private LocalDate dateOfBirth;
    private boolean gender;
}
