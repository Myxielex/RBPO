package com.example.recycling.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class LoginRequest {
    @NotBlank(message = "Укажите логин")
    private String username;

    @NotBlank(message = "Укажите пароль")
    private String password;
}
