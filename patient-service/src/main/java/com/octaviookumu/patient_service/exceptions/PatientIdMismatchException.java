package com.octaviookumu.patient_service.exceptions;

public class PatientIdMismatchException extends RuntimeException {
    public PatientIdMismatchException(String message) {
        super(message);
    }
}
