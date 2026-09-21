package com.octaviookumu.patient_service.mappers;

import com.octaviookumu.patient_service.domain.dtos.PatientResponseDto;
import com.octaviookumu.patient_service.domain.entities.Patient;

public class PatientMapper {
    public static PatientResponseDto toDto(Patient patient) {
        return PatientResponseDto.builder()
                .id(patient.getId())
                .name(patient.getName())
                .email(patient.getEmail())
                .address(patient.getAddress())
                .dateOfBirth(patient.getDateOfBirth().toString())
                .build();
    }
}
