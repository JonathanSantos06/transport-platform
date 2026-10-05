package com.transport.driver.controller;

import com.transport.driver.dto.DriverCreateRequest;
import com.transport.driver.dto.DriverResponse;
import com.transport.driver.service.DriverService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/drivers")
public class DriverController {

    private final DriverService driverService;

    public DriverController(DriverService driverService) {
        this.driverService = driverService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DriverResponse create(
            @Valid @RequestBody DriverCreateRequest request
    ) {
        return driverService.create(request);
    }

    @GetMapping("/active")
    public List<DriverResponse> findActiveDrivers() {
        return driverService.findActiveDrivers();
    }

    @GetMapping("/{id}")
    public DriverResponse findById(
            @PathVariable UUID id
    ) {
        return driverService.findById(id);
    }
}