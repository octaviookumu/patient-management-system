package com.octaviookumu.patient_service.service;

import com.octaviookumu.patient_service.domain.CreatePatientRequest;
import com.octaviookumu.patient_service.domain.UpdatePatientRequest;
import com.octaviookumu.patient_service.domain.entity.Patient;

import java.util.List;
import java.util.UUID;

public interface PatientService {
    List<Patient> getPatients();

    Patient createPatient(CreatePatientRequest createPatientRequest);

    Patient updatePatient(UUID id, UpdatePatientRequest updatePatientRequest);

    void deletePatient(UUID id);
}
