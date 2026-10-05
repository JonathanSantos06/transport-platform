package com.transport.driver.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DriverCreateRequest(

        @NotBlank(message = "Name is required")
        String name,

        @NotBlank(message = "License number is required")
        String licenseNumber,

        @NotNull(message = "Active is required")
        Boolean active

) {
}