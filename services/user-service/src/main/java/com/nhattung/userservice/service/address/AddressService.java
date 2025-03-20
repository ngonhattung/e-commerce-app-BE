package com.nhattung.userservice.service.address;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhattung.userservice.dto.AddressDto;
import com.nhattung.userservice.entity.Address;
import com.nhattung.userservice.entity.UserProfile;
import com.nhattung.userservice.exception.AppException;
import com.nhattung.userservice.exception.ErrorCode;
import com.nhattung.userservice.repository.AddressRepository;
import com.nhattung.userservice.repository.UserProfileRepository;
import com.nhattung.userservice.request.CreateAddressRequest;
import com.nhattung.userservice.request.UpdateAddressRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AddressService implements IAddressService {

    private final AddressRepository addressRepository;
    private final UserProfileRepository userProfileRepository;
    private final ObjectMapper objectMapper;
    @Override
    public Address saveAddress(CreateAddressRequest request) {

        UserProfile userProfile = userProfileRepository.findById(request.getUserId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        Address address = Address.builder()
                .city(request.getCity())
                .district(request.getDistrict())
                .street(request.getStreet())
                .user(userProfile)
                .build();
        return addressRepository.save(address);
    }

    @Override
    public Address updateAddress(UpdateAddressRequest request, Long id) {
        return addressRepository.findById(id)
                .map(address -> {
                    address.setCity(request.getCity());
                    address.setDistrict(request.getDistrict());
                    address.setStreet(request.getStreet());
                    return addressRepository.save(address);
                }).orElseThrow(() -> new RuntimeException("Address not found"));
    }

    @Override
    public void deleteAddress(Long id) {
        addressRepository.deleteById(id);
    }

    @Override
    public List<Address> getAllAddressByUserId(Long userId) {
        return addressRepository.findAllByUserId(userId);
    }

    @Override
    public AddressDto convertToDto(Address address) {
        return objectMapper.convertValue(address, AddressDto.class);
    }

    @Override
    public List<AddressDto> convertToDtos(List<Address> addresses) {
        return addresses.stream()
                .map(this::convertToDto)
                .toList();
    }


}
