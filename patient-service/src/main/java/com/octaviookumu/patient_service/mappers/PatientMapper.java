package com.octaviookumu.patient_service.mappers;

import com.octaviookumu.patient_service.domain.CreatePatientRequest;
import com.octaviookumu.patient_service.domain.UpdatePatientRequest;
import com.octaviookumu.patient_service.domain.dtos.CreatePatientRequestDto;
import com.octaviookumu.patient_service.domain.dtos.PatientResponseDto;
import com.octaviookumu.patient_service.domain.dtos.UpdatePatientRequestDto;
import com.octaviookumu.patient_service.domain.entities.Patient;

import java.time.LocalDate;

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

    public static CreatePatientRequest toCreatePatientRequest(CreatePatientRequestDto createPatientRequestDto) {
        return CreatePatientRequest.builder()
                .name(createPatientRequestDto.getName())
                .email(createPatientRequestDto.getEmail())
                .address(createPatientRequestDto.getAddress())
                .dateOfBirth(createPatientRequestDto.getDateOfBirth())
                .registeredDate(createPatientRequestDto.getRegisteredDate())
                .build();
    }

    public static Patient toModel(CreatePatientRequest createPatientRequest) {
        return Patient.builder()
                .name(createPatientRequest.getName())
                .email(createPatientRequest.getEmail())
                .address(createPatientRequest.getAddress())
                .dateOfBirth(LocalDate.parse(createPatientRequest.getDateOfBirth()))
                .registeredDate(LocalDate.parse(createPatientRequest.getRegisteredDate()))
                .build();
    }

    public static UpdatePatientRequest toUpdatePatientRequest(
            UpdatePatientRequestDto updatePatientRequestDto) {
        return UpdatePatientRequest.builder()
                .id(updatePatientRequestDto.getId())
                .name(updatePatientRequestDto.getName())
                .email(updatePatientRequestDto.getEmail())
                .address(updatePatientRequestDto.getAddress())
                .dateOfBirth(updatePatientRequestDto.getDateOfBirth())
                .build();
    }

}
