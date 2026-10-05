package com.transport.order.dto;

import jakarta.validation.constraints.NotBlank;

public record OrderCreateRequest(

        @NotBlank(message = "Origin is required")
        String origin,

        @NotBlank(message = "Destination is required")
        String destination
) {
}