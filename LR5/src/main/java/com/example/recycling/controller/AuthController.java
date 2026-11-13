package com.example.recycling.controller;

import com.example.recycling.dto.LoginRequest;
import com.example.recycling.dto.RefreshRequest;
import com.example.recycling.dto.RegisterUserRequest;
import com.example.recycling.dto.TokenPairResponse;
import com.example.recycling.service.UserSessionService;
import com.example.recycling.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {
    private final UserSessionService sessionService;
    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request, HttpServletRequest http) {
        TokenPairResponse pair = sessionService.login(request.getUsername(), request.getPassword(), deviceId(http));
        if (pair == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new MessageResponse("Неверный логин или пароль"));
        return ResponseEntity.ok(pair);
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterUserRequest request, HttpServletRequest http) {
        TokenPairResponse pair = sessionService.register(request, userService, deviceId(http));
        return ResponseEntity.status(HttpStatus.CREATED).body(pair);
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@Valid @RequestBody RefreshRequest request) {
        TokenPairResponse pair = sessionService.refresh(request.getRefreshToken());
        if (pair == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new MessageResponse("Refresh-токен недействителен, использован или отозван"));
        return ResponseEntity.ok(pair);
    }

    private String deviceId(HttpServletRequest request) {
        String value = request.getHeader("X-Device-Id");
        return value == null || value.isBlank() ? null : value.trim();
    }

    public record MessageResponse(String message) {}
}
