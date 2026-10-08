package com.octaviookumu.auth_service.services.impl;

import com.octaviookumu.auth_service.domain.entities.User;
import com.octaviookumu.auth_service.services.AuthService;
import com.octaviookumu.auth_service.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserService userService;

    @Override
    public Optional<String> authenticate(String email, String password) {
        Optional<User> user = userService.findByEmail(email);


    }
}
