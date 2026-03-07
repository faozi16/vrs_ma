package com.af.vrs.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AuthResponseDto {
    private String tokenType;
    private String accessToken;
    private Long expiresInSeconds;
    private String username;
    private String role;
    private Long customerId;
}
