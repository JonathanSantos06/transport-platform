package com.transport.order.exception;

import com.transport.order.entity.OrderStatus;

public class InvalidOrderStatusException extends RuntimeException {

    public InvalidOrderStatusException(
            OrderStatus currentStatus,
            OrderStatus requestedStatus
    ) {
        super(
                "Invalid status transition from "
                        + currentStatus
                        + " to "
                        + requestedStatus
        );
    }
}