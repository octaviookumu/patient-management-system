package com.octaviookumu.patient_service.controllers;

import com.octaviookumu.patient_service.domain.dtos.CreatePatientRequestDto;
import com.octaviookumu.patient_service.domain.dtos.PatientResponseDto;
import com.octaviookumu.patient_service.domain.entities.Patient;
import com.octaviookumu.patient_service.mappers.PatientMapper;
import com.octaviookumu.patient_service.services.PatientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/patients")
@RequiredArgsConstructor
public class PatientController {

    private final PatientService patientService;

    @GetMapping
    public ResponseEntity<List<PatientResponseDto>> getPatients() {
        List<PatientResponseDto> patientResponseDtos = patientService.getPatients().stream()
                .map(PatientMapper::toDto).toList();
        return ResponseEntity.ok(patientResponseDtos);
    }

    @PostMapping
    public ResponseEntity<PatientResponseDto> createPatient(
            @Valid
            @RequestBody CreatePatientRequestDto createPatientRequestDto) {
        Patient savedPatient = patientService
                .createPatient(PatientMapper.toCreatePatientRequest(createPatientRequestDto));
        PatientResponseDto patientResponseDto = PatientMapper.toDto(savedPatient);
        return new ResponseEntity<>(patientResponseDto, HttpStatus.CREATED);
    }


}
