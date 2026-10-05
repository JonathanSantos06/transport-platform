package com.transport.order.controller;

import com.transport.order.dto.OrderResponse;
import com.transport.order.dto.OrderStatusUpdateRequest;
import com.transport.order.entity.OrderStatus;
import com.transport.order.exception.GlobalExceptionHandler;
import com.transport.order.exception.InvalidOrderStatusException;
import com.transport.order.exception.OrderNotFoundException;
import com.transport.order.security.JwtAuthenticationFilter;
import com.transport.order.service.OrderFileService;
import com.transport.order.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrderService orderService;

    @MockBean
    private OrderFileService orderFileService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void shouldCreateOrder() throws Exception {

        UUID id = UUID.randomUUID();

        OrderResponse response = new OrderResponse(
                id,
                OrderStatus.CREATED,
                "Mexico City",
                "Guadalajara",
                LocalDateTime.now(),
                LocalDateTime.now(),
                null
        );

        when(orderService.create(org.mockito.ArgumentMatchers.any()))
                .thenReturn(response);

        String request = """
                {
                    "origin": "Mexico City",
                    "destination": "Guadalajara"
                }
                """;

        mockMvc.perform(
                        post("/api/orders")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.status").value("CREATED"))
                .andExpect(jsonPath("$.origin").value("Mexico City"))
                .andExpect(jsonPath("$.destination").value("Guadalajara"));
    }

    @Test
    void shouldFindAllOrders() throws Exception {

        UUID id = UUID.randomUUID();

        OrderResponse response = new OrderResponse(
                id,
                OrderStatus.CREATED,
                "Mexico City",
                "Monterrey",
                LocalDateTime.now(),
                LocalDateTime.now(),
                null
        );

        when(orderService.findAll(
                null,
                null,
                null,
                null,
                null
        )).thenReturn(List.of(response));

        mockMvc.perform(
                        get("/api/orders")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(id.toString()))
                .andExpect(jsonPath("$[0].status").value("CREATED"));
    }

    @Test
    void shouldFindOrderById() throws Exception {

        UUID id = UUID.randomUUID();

        OrderResponse response = new OrderResponse(
                id,
                OrderStatus.CREATED,
                "Mexico City",
                "Puebla",
                LocalDateTime.now(),
                LocalDateTime.now(),
                null
        );

        when(orderService.findById(id))
                .thenReturn(response);

        mockMvc.perform(
                        get("/api/orders/{id}", id)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.status").value("CREATED"));
    }

    @Test
    void shouldUpdateOrderStatus() throws Exception {

        UUID id = UUID.randomUUID();

        OrderResponse response = new OrderResponse(
                id,
                OrderStatus.IN_TRANSIT,
                "Mexico City",
                "Guadalajara",
                LocalDateTime.now(),
                LocalDateTime.now(),
                null
        );

        when(orderService.updateStatus(
                org.mockito.ArgumentMatchers.eq(id),
                org.mockito.ArgumentMatchers.eq(OrderStatus.IN_TRANSIT)
        )).thenReturn(response);

        OrderStatusUpdateRequest request =
                new OrderStatusUpdateRequest(OrderStatus.IN_TRANSIT);

        mockMvc.perform(
                        patch("/api/orders/{id}/status", id)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_TRANSIT"));
    }

    @Test
    void shouldReturnNotFoundWhenOrderDoesNotExist() throws Exception {

        UUID id = UUID.randomUUID();

        when(orderService.findById(id))
                .thenThrow(new OrderNotFoundException(id));

        mockMvc.perform(
                        get("/api/orders/{id}", id)
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("ORDER_NOT_FOUND"))
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void shouldReturnConflictWhenStatusTransitionIsInvalid() throws Exception {

        UUID id = UUID.randomUUID();

        when(orderService.updateStatus(
                org.mockito.ArgumentMatchers.eq(id),
                org.mockito.ArgumentMatchers.eq(OrderStatus.DELIVERED)
        )).thenThrow(
                new InvalidOrderStatusException(
                        OrderStatus.CREATED,
                        OrderStatus.DELIVERED
                )
        );

        String request = """
                {
                    "status": "DELIVERED"
                }
                """;

        mockMvc.perform(
                        patch("/api/orders/{id}/status", id)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request)
                )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error")
                        .value("INVALID_ORDER_STATUS"))
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void shouldReturnBadRequestWhenRequestIsInvalid() throws Exception {

        String request = """
                {
                    "origin": "",
                    "destination": ""
                }
                """;

        mockMvc.perform(
                        post("/api/orders")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error")
                        .value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.status").value(400));
    }
}