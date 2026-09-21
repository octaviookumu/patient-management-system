package com.octaviookumu.patient_service.domain;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class CreatePatientRequest {
    private String name;
    private String email;
    private String address;
    private String dateOfBirth;
    private String registeredDate;
}

// got this from devtiro's blog project
// (i.e. passing CreatePatientRequest to the service layer instead of CreatePatientRequestDto)
