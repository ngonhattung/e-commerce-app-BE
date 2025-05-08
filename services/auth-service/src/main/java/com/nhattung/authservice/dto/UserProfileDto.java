package com.nhattung.authservice.dto;


import lombok.Data;

import java.time.LocalDate;

@Data
public class UserProfileDto {

    private String userId;
    private String fullName;
    private String phone;
    private String avatar;
    private String email;
    private LocalDate dateOfBirth;
    private boolean gender;


}
