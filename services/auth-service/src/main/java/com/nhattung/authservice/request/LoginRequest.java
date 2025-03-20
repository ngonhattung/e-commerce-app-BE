package com.nhattung.authservice.request;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequest {

    @NotBlank(message = "EMPTY_EMAIL")
    @Email(message = "INVALID_EMAIL")
    private String username;

    @NotBlank(message = "EMPTY_PASSWORD")
    @Size(min = 8,message = "INVALID_PASSWORD")
    private String password;
}
