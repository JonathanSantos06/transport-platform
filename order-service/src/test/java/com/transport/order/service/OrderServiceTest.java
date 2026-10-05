package com.transport.order.service;

import com.transport.order.client.DriverClient;
import com.transport.order.dto.OrderCreateRequest;
import com.transport.order.dto.OrderResponse;
import com.transport.order.entity.Order;
import com.transport.order.entity.OrderStatus;
import com.transport.order.exception.DriverAssignmentException;
import com.transport.order.exception.InvalidOrderStatusException;
import com.transport.order.exception.OrderNotFoundException;
import com.transport.order.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private DriverClient driverClient;

    private OrderService orderService;

    @BeforeEach
    void setUp() {
        orderService = new OrderService(
                orderRepository,
                driverClient
        );
    }

    @Test
    void shouldCreateOrderWithCreatedStatus() {

        OrderCreateRequest request = new OrderCreateRequest(
                "Mexico City",
                "Guadalajara"
        );

        Order savedOrder = new Order(
                UUID.randomUUID(),
                OrderStatus.CREATED,
                request.origin(),
                request.destination(),
                null,
                null,
                null
        );

        when(orderRepository.save(any(Order.class)))
                .thenReturn(savedOrder);

        OrderResponse response = orderService.create(request);

        assertNotNull(response);
        assertEquals(OrderStatus.CREATED, response.status());
        assertEquals("Mexico City", response.origin());
        assertEquals("Guadalajara", response.destination());

        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void shouldFindOrderById() {

        UUID orderId = UUID.randomUUID();

        Order order = new Order(
                orderId,
                OrderStatus.CREATED,
                "Mexico City",
                "Monterrey",
                null,
                null,
                null
        );

        when(orderRepository.findById(orderId))
                .thenReturn(Optional.of(order));

        OrderResponse response = orderService.findById(orderId);

        assertNotNull(response);
        assertEquals(orderId, response.id());
        assertEquals(OrderStatus.CREATED, response.status());

        verify(orderRepository).findById(orderId);
    }

    @Test
    void shouldThrowExceptionWhenOrderDoesNotExist() {

        UUID orderId = UUID.randomUUID();

        when(orderRepository.findById(orderId))
                .thenReturn(Optional.empty());

        assertThrows(
                OrderNotFoundException.class,
                () -> orderService.findById(orderId)
        );

        verify(orderRepository).findById(orderId);
    }

    @Test
    void shouldChangeCreatedToInTransit() {

        UUID orderId = UUID.randomUUID();

        Order order = new Order(
                orderId,
                OrderStatus.CREATED,
                "Mexico City",
                "Guadalajara",
                null,
                null,
                null
        );

        when(orderRepository.findById(orderId))
                .thenReturn(Optional.of(order));

        when(orderRepository.save(order))
                .thenReturn(order);

        OrderResponse response = orderService.updateStatus(
                orderId,
                OrderStatus.IN_TRANSIT
        );

        assertEquals(
                OrderStatus.IN_TRANSIT,
                response.status()
        );
    }

    @Test
    void shouldChangeCreatedToCancelled() {

        UUID orderId = UUID.randomUUID();

        Order order = new Order(
                orderId,
                OrderStatus.CREATED,
                "Mexico City",
                "Puebla",
                null,
                null,
                null
        );

        when(orderRepository.findById(orderId))
                .thenReturn(Optional.of(order));

        when(orderRepository.save(order))
                .thenReturn(order);

        OrderResponse response = orderService.updateStatus(
                orderId,
                OrderStatus.CANCELLED
        );

        assertEquals(
                OrderStatus.CANCELLED,
                response.status()
        );
    }

    @Test
    void shouldChangeInTransitToDelivered() {

        UUID orderId = UUID.randomUUID();

        Order order = new Order(
                orderId,
                OrderStatus.IN_TRANSIT,
                "Mexico City",
                "Monterrey",
                null,
                null,
                null
        );

        when(orderRepository.findById(orderId))
                .thenReturn(Optional.of(order));

        when(orderRepository.save(order))
                .thenReturn(order);

        OrderResponse response = orderService.updateStatus(
                orderId,
                OrderStatus.DELIVERED
        );

        assertEquals(
                OrderStatus.DELIVERED,
                response.status()
        );
    }

    @Test
    void shouldChangeInTransitToCancelled() {

        UUID orderId = UUID.randomUUID();

        Order order = new Order(
                orderId,
                OrderStatus.IN_TRANSIT,
                "Mexico City",
                "Queretaro",
                null,
                null,
                null
        );

        when(orderRepository.findById(orderId))
                .thenReturn(Optional.of(order));

        when(orderRepository.save(order))
                .thenReturn(order);

        OrderResponse response = orderService.updateStatus(
                orderId,
                OrderStatus.CANCELLED
        );

        assertEquals(
                OrderStatus.CANCELLED,
                response.status()
        );
    }

    @Test
    void shouldRejectCreatedToDelivered() {

        UUID orderId = UUID.randomUUID();

        Order order = new Order(
                orderId,
                OrderStatus.CREATED,
                "Mexico City",
                "Cancun",
                null,
                null,
                null
        );

        when(orderRepository.findById(orderId))
                .thenReturn(Optional.of(order));

        assertThrows(
                InvalidOrderStatusException.class,
                () -> orderService.updateStatus(
                        orderId,
                        OrderStatus.DELIVERED
                )
        );

        verify(orderRepository, never()).save(any());
    }

    @Test
    void shouldRejectDeliveredToInTransit() {

        UUID orderId = UUID.randomUUID();

        Order order = new Order(
                orderId,
                OrderStatus.DELIVERED,
                "Mexico City",
                "Tijuana",
                null,
                null,
                null
        );

        when(orderRepository.findById(orderId))
                .thenReturn(Optional.of(order));

        assertThrows(
                InvalidOrderStatusException.class,
                () -> orderService.updateStatus(
                        orderId,
                        OrderStatus.IN_TRANSIT
                )
        );

        verify(orderRepository, never()).save(any());
    }

    @Test
    void shouldRejectCancelledToCreated() {

        UUID orderId = UUID.randomUUID();

        Order order = new Order(
                orderId,
                OrderStatus.CANCELLED,
                "Mexico City",
                "Merida",
                null,
                null,
                null
        );

        when(orderRepository.findById(orderId))
                .thenReturn(Optional.of(order));

        assertThrows(
                InvalidOrderStatusException.class,
                () -> orderService.updateStatus(
                        orderId,
                        OrderStatus.CREATED
                )
        );

        verify(orderRepository, never()).save(any());
    }

    @Test
    void shouldAssignActiveDriverToCreatedOrder() {

        UUID orderId = UUID.randomUUID();
        UUID driverId = UUID.randomUUID();

        Order order = new Order(
                orderId,
                OrderStatus.CREATED,
                "Ciudad de México",
                "Guadalajara",
                null,
                null,
                null
        );

        DriverClient.DriverResponse driver =
                new DriverClient.DriverResponse(
                        driverId,
                        "Carlos Rodriguez",
                        "LIC-001",
                        true
                );

        when(orderRepository.findById(orderId))
                .thenReturn(Optional.of(order));

        when(driverClient.findById(driverId))
                .thenReturn(driver);

        when(orderRepository.save(order))
                .thenReturn(order);

        OrderResponse response = orderService.assignDriver(
                orderId,
                driverId
        );

        assertEquals(driverId, response.driverId());

        verify(driverClient).findById(driverId);
        verify(orderRepository).save(order);
    }

    @Test
    void shouldRejectInactiveDriver() {

        UUID orderId = UUID.randomUUID();
        UUID driverId = UUID.randomUUID();

        Order order = new Order(
                orderId,
                OrderStatus.CREATED,
                "Ciudad de México",
                "Guadalajara",
                null,
                null,
                null
        );

        DriverClient.DriverResponse driver =
                new DriverClient.DriverResponse(
                        driverId,
                        "Carlos Rodriguez",
                        "LIC-001",
                        false
                );

        when(orderRepository.findById(orderId))
                .thenReturn(Optional.of(order));

        when(driverClient.findById(driverId))
                .thenReturn(driver);

        assertThrows(
                DriverAssignmentException.class,
                () -> orderService.assignDriver(
                        orderId,
                        driverId
                )
        );

        verify(orderRepository, never()).save(any());
    }

    @Test
    void shouldRejectDriverAssignmentWhenOrderIsInTransit() {

        UUID orderId = UUID.randomUUID();
        UUID driverId = UUID.randomUUID();

        Order order = new Order(
                orderId,
                OrderStatus.IN_TRANSIT,
                "Ciudad de México",
                "Monterrey",
                null,
                null,
                null
        );

        when(orderRepository.findById(orderId))
                .thenReturn(Optional.of(order));

        assertThrows(
                DriverAssignmentException.class,
                () -> orderService.assignDriver(
                        orderId,
                        driverId
                )
        );

        verify(driverClient, never()).findById(any());
        verify(orderRepository, never()).save(any());
    }

    @Test
    void shouldRejectDriverAssignmentWhenOrderIsDelivered() {

        UUID orderId = UUID.randomUUID();
        UUID driverId = UUID.randomUUID();

        Order order = new Order(
                orderId,
                OrderStatus.DELIVERED,
                "Ciudad de México",
                "Puebla",
                null,
                null,
                null
        );

        when(orderRepository.findById(orderId))
                .thenReturn(Optional.of(order));

        assertThrows(
                DriverAssignmentException.class,
                () -> orderService.assignDriver(
                        orderId,
                        driverId
                )
        );

        verify(driverClient, never()).findById(any());
        verify(orderRepository, never()).save(any());
    }

    @Test
    void shouldRejectDriverAssignmentWhenOrderIsCancelled() {

        UUID orderId = UUID.randomUUID();
        UUID driverId = UUID.randomUUID();

        Order order = new Order(
                orderId,
                OrderStatus.CANCELLED,
                "Ciudad de México",
                "Tijuana",
                null,
                null,
                null
        );

        when(orderRepository.findById(orderId))
                .thenReturn(Optional.of(order));

        assertThrows(
                DriverAssignmentException.class,
                () -> orderService.assignDriver(
                        orderId,
                        driverId
                )
        );

        verify(driverClient, never()).findById(any());
        verify(orderRepository, never()).save(any());
    }
}