package com.meetingroom.dto;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TokenResponse {
    private String accessToken;
    private String tokenType;
    private long expiresIn;
    private String refreshToken;

    // Custom constructor for login registration
    public TokenResponse(String accessToken) {
        this.accessToken = accessToken;
        this.tokenType = "Bearer";
        this.expiresIn = 3600;
        this.refreshToken = null;
    }
}
