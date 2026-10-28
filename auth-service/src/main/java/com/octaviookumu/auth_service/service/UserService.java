package com.octaviookumu.auth_service.service;

import com.octaviookumu.auth_service.domain.entities.User;

import java.util.Optional;

public interface UserService {
    Optional<User> findByEmail(String email);
}
