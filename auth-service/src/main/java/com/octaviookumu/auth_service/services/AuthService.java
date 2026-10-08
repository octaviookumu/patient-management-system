package com.octaviookumu.auth_service.services;

import java.util.Optional;

public interface AuthService {
    Optional<String> authenticate(String email, String password);

    boolean validateToken(String token);
}
