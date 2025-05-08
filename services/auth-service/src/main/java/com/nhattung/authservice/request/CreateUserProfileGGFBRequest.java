package com.nhattung.authservice.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateUserProfileGGFBRequest {
    private String userId;
    private String fullName;
    private String email;
    private String avatar;
}
