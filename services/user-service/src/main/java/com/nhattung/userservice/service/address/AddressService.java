package com.nhattung.userservice.service.address;

import com.nhattung.userservice.entity.Address;
import com.nhattung.userservice.repository.AddressRepository;
import com.nhattung.userservice.repository.UserProfileRepository;
import com.nhattung.userservice.request.CreateAddressRequest;
import com.nhattung.userservice.request.UpdateAddressRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AddressService implements IAddressService {

    private final AddressRepository addressRepository;
    private final UserProfileRepository userProfileRepository;

    @Override
    public Address saveAddress(CreateAddressRequest request) {
        Address address = Address.builder()
                .city(request.getCity())
                .district(request.getDistrict())
                .street(request.getStreet())
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

    }



}
