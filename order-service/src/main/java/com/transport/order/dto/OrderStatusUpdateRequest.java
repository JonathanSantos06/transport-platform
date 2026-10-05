package com.transport.order.dto;

import com.transport.order.entity.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record OrderStatusUpdateRequest(

        @NotNull(message = "Status is required")
        OrderStatus status

) {
}