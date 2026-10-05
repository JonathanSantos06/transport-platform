package com.transport.driver.exception;

public class DriverAlreadyExistsException extends RuntimeException {

    public DriverAlreadyExistsException(String licenseNumber) {
        super(
                "A driver with license number "
                        + licenseNumber
                        + " already exists"
        );
    }
}