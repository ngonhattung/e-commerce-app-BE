package com.nhattung.userservice.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateAddressRequest {

    @NotBlank(message = "EMPTY_STREET")
    @Size(min = 3, max = 100,message = "INVALID_STREET")
    private String street;

    @NotBlank(message = "EMPTY_DISTRICT")
    @Size(min = 2, max = 50,message = "INVALID_DISTRICT")
    private String district;

    @NotBlank(message = "EMPTY_CITY")
    @Size(min = 2, max = 50,message = "INVALID_CITY")
    private String city;
}
