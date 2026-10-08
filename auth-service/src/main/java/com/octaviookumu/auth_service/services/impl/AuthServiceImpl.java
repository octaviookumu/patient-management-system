package com.octaviookumu.auth_service.services.impl;

import com.octaviookumu.auth_service.domain.entities.User;
import com.octaviookumu.auth_service.services.AuthService;
import com.octaviookumu.auth_service.services.UserService;
import com.octaviookumu.auth_service.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    public Optional<String> authenticate(String email, String password) {
        Optional<String> token = userService.findByEmail(email)
                .filter(u -> passwordEncoder.matches(password, u.getPassword()))
                .map(u -> jwtUtil.generateToken(u.getEmail(), u.getRole()));

        // passwordEncoder.matches() - if the password in the login request matches what is stored for the user
        // jwtUtil.generateToken() - if the passwords are valid, generate a token using user's email and role
        // This transforms the user into a token

        return token;
    }
}
