package com.nhattung.userservice.service.address;

import com.nhattung.userservice.dto.AddressDto;
import com.nhattung.userservice.entity.Address;
import com.nhattung.userservice.request.CreateAddressRequest;
import com.nhattung.userservice.request.UpdateAddressRequest;

import java.util.List;

public interface IAddressService {
    Address saveAddress(CreateAddressRequest request);
    Address updateAddress(UpdateAddressRequest request, Long id);
    void deleteAddress(Long id);
    List<Address> getAllAddressByUserId();
    AddressDto convertToDto(Address address);
    List<AddressDto> convertToDtos(List<Address> addresses);

}
