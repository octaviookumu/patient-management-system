package com.octaviookumu.patient_service.domain.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiErrorResponseDto {
    private int status;
    private String message;
    private List<FieldError> errors;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FieldError { // Changed to public so it's accessible in the handler
        private String field;
        private String message;
    }

    // Using a List<FieldError> object instead of a raw Map
    // allows you to handle multiple validation errors on the same field
    // if that ever happens down the line.

}
