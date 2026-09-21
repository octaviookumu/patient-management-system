package com.octaviookumu.patient_service.services.impl;

import com.octaviookumu.patient_service.domain.entities.Patient;
import com.octaviookumu.patient_service.repositories.PatientRepository;
import com.octaviookumu.patient_service.services.PatientService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PatientServiceImpl implements PatientService {

    private final PatientRepository patientRepository;

    @Override
    public List<Patient> getPatients() {
        return patientRepository.findAll();
    }
}
