package com.octaviookumu.patient_service.services.impl;

import com.octaviookumu.patient_service.domain.CreatePatientRequest;
import com.octaviookumu.patient_service.domain.entities.Patient;
import com.octaviookumu.patient_service.mappers.PatientMapper;
import com.octaviookumu.patient_service.repositories.PatientRepository;
import com.octaviookumu.patient_service.services.PatientService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PatientServiceImpl implements PatientService {

    private final PatientRepository patientRepository;

    @Override
    public List<Patient> getPatients() {
        return patientRepository.findAll();
    }

    @Transactional
    @Override
    public Patient createPatient(CreatePatientRequest createPatientRequest) {
        String email = createPatientRequest.getEmail();
        patientRepository.findByEmail(email).ifPresent(existing -> {
            throw new IllegalStateException("Patient with email " + email + " already exists");
        });

        Patient newPatient = PatientMapper.toPatient(createPatientRequest);

        return patientRepository.save(newPatient);
    }
}
