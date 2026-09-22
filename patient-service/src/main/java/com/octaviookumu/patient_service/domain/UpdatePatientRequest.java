package com.octaviookumu.patient_service.domain;

import lombok.*;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class UpdatePatientRequest {
    private UUID id;
    private String name;
    private String email;
    private String address;
    private String dateOfBirth;
}
