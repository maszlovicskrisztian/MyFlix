package com.maszlovicskrisztian.myflix_core.controller;

import com.maszlovicskrisztian.myflix_core.dtos.LoginRequest;
import com.maszlovicskrisztian.myflix_core.dtos.LoginResponse;
import com.maszlovicskrisztian.myflix_core.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    @Value("${ADMIN_USER}")
    private String adminUser;

    @Value("${ADMIN_PASSWORD}")
    private String adminPassword;

    private final JwtService jwtService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        if (!adminUser.equals(request.username()) || !adminPassword.equals(request.password())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }

        String token = jwtService.generateToken(request.username());
        return ResponseEntity.ok(new LoginResponse(token));
    }
}
