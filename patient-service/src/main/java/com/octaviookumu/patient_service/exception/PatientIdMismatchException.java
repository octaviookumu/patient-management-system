package com.octaviookumu.patient_service.exception;

public class PatientIdMismatchException extends RuntimeException {
    public PatientIdMismatchException(String message) {
        super(message);
    }
}
