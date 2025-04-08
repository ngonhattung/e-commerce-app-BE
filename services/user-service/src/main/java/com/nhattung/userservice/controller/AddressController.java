package com.nhattung.userservice.controller;

import com.nhattung.userservice.dto.AddressDto;
import com.nhattung.userservice.entity.Address;
import com.nhattung.userservice.request.CreateAddressRequest;
import com.nhattung.userservice.request.UpdateAddressRequest;
import com.nhattung.userservice.response.ApiResponse;
import com.nhattung.userservice.service.address.IAddressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user-address")
public class AddressController {

    private final IAddressService addressService;

    @GetMapping("/get")
    public ApiResponse<List<AddressDto>> getAddressByUserID() {
        List<Address> address = addressService.getAllAddressByUserId();
        List<AddressDto> addressDto = addressService.convertToDtos(address);
        return ApiResponse.<List<AddressDto>>builder()
                .message("Address found successfully")
                .result(addressDto)
                .build();
    }

    @PostMapping("/add")
    public ApiResponse<AddressDto> addAddress(@Valid @RequestBody CreateAddressRequest request) {
        Address address = addressService.saveAddress(request);
        AddressDto addressDto = addressService.convertToDto(address);
        return ApiResponse.<AddressDto>builder()
                .message("Address added successfully")
                .result(addressDto)
                .build();
    }

    @PutMapping("/update/{id}")
    public ApiResponse<AddressDto> updateAddress(@RequestBody UpdateAddressRequest request,
                                                 @PathVariable Long id) {
        Address address = addressService.updateAddress(request, id);
        AddressDto addressDto = addressService.convertToDto(address);
        return ApiResponse.<AddressDto>builder()
                .message("Address updated successfully")
                .result(addressDto)
                .build();
    }

    @DeleteMapping("/delete/{id}")
    public ApiResponse<String> deleteAddress(@PathVariable Long id) {
        addressService.deleteAddress(id);
        return ApiResponse.<String>builder()
                .result("Address deleted successfully")
                .build();
    }

}
