package com.example.recycling.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TokenPairResponse {
    private String accessToken;
    private String refreshToken;
}
