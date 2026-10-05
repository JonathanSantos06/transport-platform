package com.transport.order.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AssignDriverRequest(

        @NotNull(message = "Driver id is required")
        UUID driverId

) {
}