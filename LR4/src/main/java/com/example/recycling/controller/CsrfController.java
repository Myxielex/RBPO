package com.example.recycling.controller;

import com.example.recycling.dto.CsrfTokenResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CsrfController {

    @GetMapping("/api/csrf-token")
    public ResponseEntity<CsrfTokenResponse> getCsrfToken(CsrfToken csrfToken) {
        System.out.println("CSRF CONTROLLER CALLED");
        return ResponseEntity.ok(
                new CsrfTokenResponse(csrfToken.getHeaderName(), csrfToken.getToken()));
    }
}
