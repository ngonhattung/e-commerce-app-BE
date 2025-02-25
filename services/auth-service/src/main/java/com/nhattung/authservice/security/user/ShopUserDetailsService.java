package com.nhattung.authservice.security.user;


import com.nhattung.authservice.entity.User;
import com.nhattung.authservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ShopUserDetailsService implements UserDetailsService { //Lấy thông tin user từ database

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String input) throws UsernameNotFoundException {
        User user = Optional.ofNullable(userRepository.findByUsernameOrEmailOrPhone(input, input,input))
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return ShopUserDetails.buildUserDetail(user);
    }

}
