package com.octaviookumu.patient_service.domain.dtos;

import lombok.*;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class PatientResponseDto {
    private UUID id;
    private String name;
    private String email;
    private String address;
    private String dateOfBirth;
}

