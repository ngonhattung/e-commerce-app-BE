package com.nhattung.userservice.service.address;

import com.nhattung.userservice.entity.Address;
import com.nhattung.userservice.request.CreateAddressRequest;
import com.nhattung.userservice.request.UpdateAddressRequest;

public interface IAddressService {
    Address saveAddress(CreateAddressRequest request);
    Address updateAddress(UpdateAddressRequest request, Long id);
    void deleteAddress(Long id);

}
