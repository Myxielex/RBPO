package com.example.recycling.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RefreshRequest {
    @NotBlank(message = "Укажите refresh-токен")
    private String refreshToken;
}
