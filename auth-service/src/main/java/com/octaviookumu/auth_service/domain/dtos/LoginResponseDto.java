package com.octaviookumu.auth_service.domain.dtos;

import lombok.*;

@AllArgsConstructor
@Getter
public class LoginResponseDto {
    private final String token;
}
