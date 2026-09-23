package com.octaviookumu.patient_service.services.impl;

import com.octaviookumu.patient_service.domain.CreatePatientRequest;
import com.octaviookumu.patient_service.domain.UpdatePatientRequest;
import com.octaviookumu.patient_service.domain.entities.Patient;
import com.octaviookumu.patient_service.exceptions.EmailAlreadyExistsException;
import com.octaviookumu.patient_service.exceptions.PatientIdMismatchException;
import com.octaviookumu.patient_service.exceptions.PatientNotFoundException;
import com.octaviookumu.patient_service.mappers.PatientMapper;
import com.octaviookumu.patient_service.repositories.PatientRepository;
import com.octaviookumu.patient_service.services.PatientService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

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

        if (patientRepository.existsByEmail(email)) {
            // custom exception makes it easy to trace in the logs
            throw new EmailAlreadyExistsException("A patient with email " + email + " already exists");
        }

        Patient newPatient = PatientMapper.toModel(createPatientRequest);

        return patientRepository.save(newPatient);
    }

    @Transactional
    @Override
    public Patient updatePatient(UUID id, UpdatePatientRequest updatePatientRequest) {
        if (!id.equals(updatePatientRequest.getId())) {
            throw new PatientIdMismatchException("Path ID does not match Request Body ID");
        }

        Patient existingPatient = patientRepository.findById(id)
                .orElseThrow(() -> new PatientNotFoundException("Patient does not exist with ID " + id));

        String email = updatePatientRequest.getEmail();

        if (patientRepository.existsByEmailAndIdNot(email, updatePatientRequest.getId())) {
            throw new EmailAlreadyExistsException("A patient with email " + email + " already exists");
        }

        existingPatient.setName(updatePatientRequest.getName());
        existingPatient.setEmail(email);
        existingPatient.setAddress(updatePatientRequest.getAddress());
        existingPatient.setDateOfBirth(LocalDate.parse(updatePatientRequest.getDateOfBirth()));

        return patientRepository.save(existingPatient);
    }

    @Transactional
    @Override
    public void deletePatient(UUID id) {
        Patient existingPatient = patientRepository.findById(id)
                .orElseThrow(() -> new PatientNotFoundException("Patient does not exist with ID " + id));

        patientRepository.delete(existingPatient);
    }
}
