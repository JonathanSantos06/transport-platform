package com.transport.driver.controller;

import com.transport.driver.dto.DriverResponse;
import com.transport.driver.exception.GlobalExceptionHandler;
import com.transport.driver.service.DriverService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DriverController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class DriverControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DriverService driverService;

    @Test
    void shouldCreateDriver() throws Exception {

        UUID id = UUID.randomUUID();

        DriverResponse response = new DriverResponse(
                id,
                "Juan Perez",
                "LIC-001",
                true
        );

        when(driverService.create(any()))
                .thenReturn(response);

        String requestBody = """
                {
                    "name": "Juan Perez",
                    "licenseNumber": "LIC-001",
                    "active": true
                }
                """;

        mockMvc.perform(
                        post("/api/drivers")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.name").value("Juan Perez"))
                .andExpect(jsonPath("$.licenseNumber").value("LIC-001"))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void shouldFindActiveDrivers() throws Exception {

        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();

        List<DriverResponse> drivers = List.of(
                new DriverResponse(
                        id1,
                        "Juan Perez",
                        "LIC-001",
                        true
                ),
                new DriverResponse(
                        id2,
                        "Pedro Lopez",
                        "LIC-002",
                        true
                )
        );

        when(driverService.findActiveDrivers())
                .thenReturn(drivers);

        mockMvc.perform(
                        get("/api/drivers/active")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Juan Perez"))
                .andExpect(jsonPath("$[1].name").value("Pedro Lopez"));
    }

    @Test
    void shouldFindDriverById() throws Exception {

        UUID id = UUID.randomUUID();

        DriverResponse response = new DriverResponse(
                id,
                "Juan Perez",
                "LIC-001",
                true
        );

        when(driverService.findById(id))
                .thenReturn(response);

        mockMvc.perform(
                        get("/api/drivers/{id}", id)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.name").value("Juan Perez"))
                .andExpect(jsonPath("$.licenseNumber").value("LIC-001"))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void shouldReturnBadRequestWhenRequestIsInvalid() throws Exception {

        String requestBody = """
                {
                    "name": "",
                    "licenseNumber": "",
                    "active": null
                }
                """;

        mockMvc.perform(
                        post("/api/drivers")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }
}