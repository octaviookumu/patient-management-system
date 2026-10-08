package com.octaviookumu.auth_service.controllers;

import com.octaviookumu.auth_service.domain.dtos.LoginRequestDto;
import com.octaviookumu.auth_service.domain.dtos.LoginResponseDto;
import com.octaviookumu.auth_service.services.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/login")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Generate token on user login")
    @PostMapping
    public ResponseEntity<LoginResponseDto> login(
            @Valid @RequestBody LoginRequestDto loginRequestDto) {

        Optional<String> tokenOptional = authService.authenticate(
                loginRequestDto.getEmail(), loginRequestDto.getPassword());

        if (tokenOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // convert tokenOptional from an optional to a string
        String token = tokenOptional.get();

        // a constructor approach works well when with a few properties in dto/object
        return ResponseEntity.ok(new LoginResponseDto(token));

    }

}
