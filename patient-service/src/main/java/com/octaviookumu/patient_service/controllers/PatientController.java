package com.octaviookumu.patient_service.controllers;

import com.octaviookumu.patient_service.domain.dtos.CreatePatientRequestDto;
import com.octaviookumu.patient_service.domain.dtos.PatientResponseDto;
import com.octaviookumu.patient_service.domain.dtos.UpdatePatientRequestDto;
import com.octaviookumu.patient_service.domain.entities.Patient;
import com.octaviookumu.patient_service.mappers.PatientMapper;
import com.octaviookumu.patient_service.services.PatientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/patients")
@RequiredArgsConstructor
@Tag(name = "Patient", description = "API for managing Patients")
public class PatientController {

    private final PatientService patientService;

    @GetMapping
    @Operation(summary = "Get Patients")
    public ResponseEntity<List<PatientResponseDto>> getPatients() {
        List<PatientResponseDto> patientResponseDtos = patientService.getPatients().stream()
                .map(PatientMapper::toDto).toList();
        return ResponseEntity.ok(patientResponseDtos);
    }

    @PostMapping
    @Operation(summary = "Create a new Patient")
    public ResponseEntity<PatientResponseDto> createPatient(
            @Valid
            @RequestBody CreatePatientRequestDto createPatientRequestDto) {
        Patient savedPatient = patientService
                .createPatient(PatientMapper.toCreatePatientRequest(createPatientRequestDto));
        PatientResponseDto patientResponseDto = PatientMapper.toDto(savedPatient);
        return new ResponseEntity<>(patientResponseDto, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a new Patient")
    public ResponseEntity<PatientResponseDto> updatePatient(
            @PathVariable UUID id,
            @Valid
            @RequestBody UpdatePatientRequestDto updatePatientRequestDto
    ) {
        Patient updatedPatient = patientService.updatePatient(
                id, PatientMapper.toUpdatePatientRequest(updatePatientRequestDto));

        return ResponseEntity.ok(PatientMapper.toDto(updatedPatient));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a new Patient")
    public ResponseEntity<Void> deletePatient(@PathVariable UUID id) {
        patientService.deletePatient(id);
        return ResponseEntity.noContent().build();
    }


}
