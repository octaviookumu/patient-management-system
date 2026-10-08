package com.octaviookumu.auth_service.services.impl;

import com.octaviookumu.auth_service.domain.entities.User;
import com.octaviookumu.auth_service.repositories.UserRepository;
import com.octaviookumu.auth_service.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
}
