package com.octaviookumu.patient_service.repositories;

import com.octaviookumu.patient_service.domain.entities.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PatientRepository extends JpaRepository<Patient, UUID> {
    boolean existsByEmail(String email);

    /**
     * Checks if another patient is already using a specific email address, excluding the patient you are currently updating.
     * IdNot(id) means - Check if this email belongs to anyone else besides the patient with this specific ID.
     * this stops JPA from incorrectly flagging the email as duplicate even if we're trying to update the same record
     *
     * @param email Email
     * @param id    Patient ID
     */
    boolean existsByEmailAndIdNot(String email, UUID id);
}
