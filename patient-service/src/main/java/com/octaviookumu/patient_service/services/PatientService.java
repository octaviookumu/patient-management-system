package com.octaviookumu.patient_service.services;

import com.octaviookumu.patient_service.domain.entities.Patient;

import java.util.List;

public interface PatientService {
    List<Patient> getPatients();
}
