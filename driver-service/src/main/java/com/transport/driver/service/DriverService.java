package com.transport.driver.service;

import com.transport.driver.dto.DriverCreateRequest;
import com.transport.driver.dto.DriverResponse;
import com.transport.driver.entity.Driver;
import com.transport.driver.exception.DriverAlreadyExistsException;
import com.transport.driver.exception.DriverNotFoundException;
import com.transport.driver.repository.DriverRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class DriverService {

    private final DriverRepository driverRepository;

    public DriverService(DriverRepository driverRepository) {
        this.driverRepository = driverRepository;
    }

    public DriverResponse create(DriverCreateRequest request) {

        if (driverRepository.existsByLicenseNumber(
                request.licenseNumber()
        )) {
            throw new DriverAlreadyExistsException(
                    request.licenseNumber()
            );
        }

        Driver driver = new Driver(
                null,
                request.name(),
                request.licenseNumber(),
                request.active()
        );

        Driver savedDriver = driverRepository.save(driver);

        return toResponse(savedDriver);
    }

    @Transactional(readOnly = true)
    public List<DriverResponse> findActiveDrivers() {

        return driverRepository
                .findAllByActiveTrue()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public DriverResponse findById(UUID id) {

        Driver driver = driverRepository
                .findById(id)
                .orElseThrow(() ->
                        new DriverNotFoundException(id)
                );

        return toResponse(driver);
    }

    private DriverResponse toResponse(Driver driver) {

        return new DriverResponse(
                driver.getId(),
                driver.getName(),
                driver.getLicenseNumber(),
                driver.isActive()
        );
    }
}