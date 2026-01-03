package com.file.manager.controller;


import com.file.manager.dto.auth.LoginRequest;
import com.file.manager.dto.auth.LoginResponse;
import com.file.manager.dto.auth.RegisterRequest;
import com.file.manager.dto.auth.RegisterResponse;
import com.file.manager.exception.AuthExcpetion;
import com.file.manager.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;


    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody @Validated LoginRequest loginRequest) {
        try {
            // Validate the login request
            LoginResponse response = authService.login(loginRequest.getEmail(), loginRequest.getPassword());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
           throw new AuthExcpetion( e.getMessage());
        }

    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@RequestBody @Validated RegisterRequest registerRequest)  {
        try {
            log.info("Received registration request for email: {}", registerRequest.getEmail());
            // Validate the registration request
            RegisterResponse response = authService.register(registerRequest);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            throw new AuthExcpetion( e.getMessage());
        }
    }

}
