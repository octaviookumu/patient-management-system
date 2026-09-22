package com.octaviookumu.patient_service.exceptions;

import com.octaviookumu.patient_service.domain.dtos.ApiErrorResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.List;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Handler for the base exception class
     * <p>
     * If we are catching a base exception, that means something has gone quite wrong
     * </p>
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponseDto> handleException(Exception ex) {
        log.error("Caught exception", ex);
        ApiErrorResponseDto error = ApiErrorResponseDto.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .message("An unexpected error occurred")
                .build();
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Catches validation errors (such as Email address not valid, Email address required)
     *
     * @param ex MethodArgumentNotValidException
     * @return ApiErrorResponseDto
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponseDto> handleException(MethodArgumentNotValidException ex) {


        // Transform Spring's FieldErrors into your custom ApiErrorResponseDto.FieldError list
        List<ApiErrorResponseDto.FieldError> validationErrors = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(error -> ApiErrorResponseDto.FieldError.builder()
                        .field(error.getField())
                        .message(error.getDefaultMessage())
                        .build())
                .toList();

        // Build the unified DTO response
        ApiErrorResponseDto errorPayload = ApiErrorResponseDto.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .message("Validation failed for one or more fields.")
                .errors(validationErrors)
                .build();

        return ResponseEntity.badRequest().body(errorPayload);
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ApiErrorResponseDto> handleEmailAlreadyExistsException(
            EmailAlreadyExistsException ex
    ) {
        ApiErrorResponseDto error = ApiErrorResponseDto.builder()
                .status(HttpStatus.CONFLICT.value())
                .message("Email address already exists")
                .build();

        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiErrorResponseDto> handleIllegalStateException(
            IllegalStateException ex) {
        log.warn("Email address already exists {} ", ex.getMessage());
        ApiErrorResponseDto error = ApiErrorResponseDto.builder()
                .status(HttpStatus.CONFLICT.value())
                .message(ex.getMessage())
                .build();
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

}
