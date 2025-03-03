package com.nhattung.userservice.request;

import lombok.Data;

@Data
public class CreateAddressRequest {
    private String street;
    private String district;
    private String city;
    private Long userId;
}
