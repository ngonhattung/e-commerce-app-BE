package com.nhattung.userservice.request;

import lombok.Data;

@Data
public class UpdateAddressRequest {

    private String street;
    private String district;
    private String city;
}
