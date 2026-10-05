package com.transport.driver.service;

import com.transport.driver.dto.DriverCreateRequest;
import com.transport.driver.dto.DriverResponse;
import com.transport.driver.entity.Driver;
import com.transport.driver.exception.DriverAlreadyExistsException;
import com.transport.driver.exception.DriverNotFoundException;
import com.transport.driver.repository.DriverRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DriverServiceTest {

    @Mock
    private DriverRepository driverRepository;

    @InjectMocks
    private DriverService driverService;

    private UUID driverId;

    @BeforeEach
    void setUp() {
        driverId = UUID.randomUUID();
    }

    @Test
    void shouldCreateDriver() {

        DriverCreateRequest request = new DriverCreateRequest(
                "Juan Perez",
                "LIC-001",
                true
        );

        Driver savedDriver = new Driver(
                driverId,
                "Juan Perez",
                "LIC-001",
                true
        );

        when(driverRepository.existsByLicenseNumber("LIC-001"))
                .thenReturn(false);

        when(driverRepository.save(any(Driver.class)))
                .thenReturn(savedDriver);

        DriverResponse response = driverService.create(request);

        assertNotNull(response);
        assertEquals(driverId, response.id());
        assertEquals("Juan Perez", response.name());
        assertEquals("LIC-001", response.licenseNumber());
        assertTrue(response.active());

        verify(driverRepository)
                .existsByLicenseNumber("LIC-001");

        verify(driverRepository)
                .save(any(Driver.class));
    }

    @Test
    void shouldRejectDuplicatedLicense() {

        DriverCreateRequest request = new DriverCreateRequest(
                "Juan Perez",
                "LIC-001",
                true
        );

        when(driverRepository.existsByLicenseNumber("LIC-001"))
                .thenReturn(true);

        assertThrows(
                DriverAlreadyExistsException.class,
                () -> driverService.create(request)
        );

        verify(driverRepository)
                .existsByLicenseNumber("LIC-001");

        verify(driverRepository, never())
                .save(any(Driver.class));
    }

    @Test
    void shouldFindActiveDrivers() {

        Driver driver1 = new Driver(
                UUID.randomUUID(),
                "Juan Perez",
                "LIC-001",
                true
        );

        Driver driver2 = new Driver(
                UUID.randomUUID(),
                "Pedro Lopez",
                "LIC-002",
                true
        );

        when(driverRepository.findAllByActiveTrue())
                .thenReturn(List.of(driver1, driver2));

        List<DriverResponse> response =
                driverService.findActiveDrivers();

        assertNotNull(response);
        assertEquals(2, response.size());

        assertEquals(
                "Juan Perez",
                response.get(0).name()
        );

        assertEquals(
                "Pedro Lopez",
                response.get(1).name()
        );

        verify(driverRepository)
                .findAllByActiveTrue();
    }

    @Test
    void shouldReturnEmptyListWhenThereAreNoActiveDrivers() {

        when(driverRepository.findAllByActiveTrue())
                .thenReturn(List.of());

        List<DriverResponse> response =
                driverService.findActiveDrivers();

        assertNotNull(response);
        assertTrue(response.isEmpty());

        verify(driverRepository)
                .findAllByActiveTrue();
    }

    @Test
    void shouldFindDriverById() {

        Driver driver = new Driver(
                driverId,
                "Juan Perez",
                "LIC-001",
                true
        );

        when(driverRepository.findById(driverId))
                .thenReturn(java.util.Optional.of(driver));

        DriverResponse response =
                driverService.findById(driverId);

        assertNotNull(response);
        assertEquals(driverId, response.id());
        assertEquals("Juan Perez", response.name());
        assertEquals("LIC-001", response.licenseNumber());
        assertTrue(response.active());

        verify(driverRepository)
                .findById(driverId);
    }

    @Test
    void shouldThrowExceptionWhenDriverDoesNotExist() {

        when(driverRepository.findById(driverId))
                .thenReturn(java.util.Optional.empty());

        assertThrows(
                DriverNotFoundException.class,
                () -> driverService.findById(driverId)
        );

        verify(driverRepository)
                .findById(driverId);
    }
}