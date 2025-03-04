package com.nhattung.authservice.controller;


import com.nhattung.authservice.dto.UserDto;
import com.nhattung.authservice.entity.User;
import com.nhattung.authservice.exception.AlreadyExistsException;
import com.nhattung.authservice.request.LoginRequest;
import com.nhattung.authservice.request.LogoutRequest;
import com.nhattung.authservice.request.RefreshRequest;
import com.nhattung.authservice.request.RegisterRequest;
import com.nhattung.authservice.response.ApiResponse;
import com.nhattung.authservice.response.JwtResponse;
import com.nhattung.authservice.security.jwt.JwtUtils;
import com.nhattung.authservice.security.user.ShopUserDetails;
import com.nhattung.authservice.security.user.ShopUserDetailsService;
import com.nhattung.authservice.service.user.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.HttpStatus.CONFLICT;

@RequiredArgsConstructor
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final ShopUserDetailsService userDetailsService;
    private final JwtUtils jwtUtils;
    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse> register(@Valid @RequestBody RegisterRequest request) {
        try {
            User user = userService.createUser(request);
            UserDto userDto = userService.convertUserToDto(user);
            return ResponseEntity.ok(new ApiResponse("Create User Success!", userDto));
        } catch (AlreadyExistsException e) {
            //409
            return ResponseEntity.status(CONFLICT).body(new ApiResponse(e.getMessage(), null));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse> login(@Valid @RequestBody LoginRequest request)
    {
        try {
            Authentication authentication = authenticationManager
                    .authenticate(new UsernamePasswordAuthenticationToken(
                            request.getEmail(), request.getPassword()));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            String accessToken = jwtUtils.generateJwtToken(authentication);
            String refreshToken = jwtUtils.createRefreshToken(authentication);
            ShopUserDetails userDetails = (ShopUserDetails) authentication.getPrincipal();
            JwtResponse jwtResponse = new JwtResponse(userDetails.getId(), accessToken, refreshToken);
            return ResponseEntity.ok(new ApiResponse("User logged in successfully", jwtResponse));
        }catch (AuthenticationException e) {
            //401
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiResponse(e.getMessage(), null));
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        try {
            if(jwtUtils.validateJwtToken(request.getToken())){
                String username = jwtUtils.getUsernameFromJwtToken(request.getToken());
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                var auth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(auth);
                ShopUserDetails userD = (ShopUserDetails) auth.getPrincipal();
                String accessToken = jwtUtils.generateJwtToken(auth);
                JwtResponse jwtResponse = new JwtResponse(userD.getId(), accessToken, request.getToken());
                return ResponseEntity.ok(new ApiResponse("Token refreshed successfully", jwtResponse));
            }else{
                //401
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ApiResponse("Invalid token", null));
            }
        }catch (AuthenticationException e) {
            //401
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse(e.getMessage(), null));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse> logout(@RequestBody LogoutRequest request) {
        jwtUtils.logout(request);
        return ResponseEntity.ok(new ApiResponse("User logged out successfully", null));
    }

}
