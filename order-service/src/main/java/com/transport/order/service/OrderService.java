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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class OrderService {

    private static final Logger log =
            LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final DriverClient driverClient;

    public OrderService(
            OrderRepository orderRepository,
            DriverClient driverClient
    ) {
        this.orderRepository = orderRepository;
        this.driverClient = driverClient;
    }

    public OrderResponse create(OrderCreateRequest request) {

        log.info(
                "Creating order origin={} destination={}",
                request.origin(),
                request.destination()
        );

        Order order = new Order(
                null,
                OrderStatus.CREATED,
                request.origin(),
                request.destination(),
                null,
                null,
                null
        );

        Order savedOrder = orderRepository.save(order);

        log.info(
                "Order created successfully orderId={} status={}",
                savedOrder.getId(),
                savedOrder.getStatus()
        );

        return toResponse(savedOrder);
    }

    @Transactional(readOnly = true)
    public OrderResponse findById(UUID id) {

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));

        return toResponse(order);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> findAll(
            OrderStatus status,
            String origin,
            String destination,
            LocalDateTime from,
            LocalDateTime to
    ) {

        Specification<Order> specification = Specification.where(null);

        if (status != null) {
            specification = specification.and(
                    (root, query, criteriaBuilder) ->
                            criteriaBuilder.equal(
                                    root.get("status"),
                                    status
                            )
            );
        }

        if (origin != null && !origin.isBlank()) {
            specification = specification.and(
                    (root, query, criteriaBuilder) ->
                            criteriaBuilder.like(
                                    criteriaBuilder.lower(
                                            root.get("origin")
                                    ),
                                    "%" + origin.toLowerCase() + "%"
                            )
            );
        }

        if (destination != null && !destination.isBlank()) {
            specification = specification.and(
                    (root, query, criteriaBuilder) ->
                            criteriaBuilder.like(
                                    criteriaBuilder.lower(
                                            root.get("destination")
                                    ),
                                    "%" + destination.toLowerCase() + "%"
                            )
            );
        }

        if (from != null) {
            specification = specification.and(
                    (root, query, criteriaBuilder) ->
                            criteriaBuilder.greaterThanOrEqualTo(
                                    root.get("createdAt"),
                                    from
                            )
            );
        }

        if (to != null) {
            specification = specification.and(
                    (root, query, criteriaBuilder) ->
                            criteriaBuilder.lessThanOrEqualTo(
                                    root.get("createdAt"),
                                    to
                            )
            );
        }

        return orderRepository.findAll(
                        specification,
                        Sort.by(
                                Sort.Direction.DESC,
                                "createdAt"
                        )
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public OrderResponse updateStatus(
            UUID id,
            OrderStatus newStatus
    ) {

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));

        log.info(
                "Updating order status orderId={} currentStatus={} requestedStatus={}",
                id,
                order.getStatus(),
                newStatus
        );

        validateStatusTransition(
                order.getStatus(),
                newStatus
        );

        order.setStatus(newStatus);

        Order updatedOrder = orderRepository.save(order);

        log.info(
                "Order status updated successfully orderId={} newStatus={}",
                updatedOrder.getId(),
                updatedOrder.getStatus()
        );

        return toResponse(updatedOrder);
    }

    private void validateStatusTransition(
            OrderStatus currentStatus,
            OrderStatus newStatus
    ) {

        boolean validTransition = switch (currentStatus) {

            case CREATED ->
                    newStatus == OrderStatus.IN_TRANSIT
                            || newStatus == OrderStatus.CANCELLED;

            case IN_TRANSIT ->
                    newStatus == OrderStatus.DELIVERED
                            || newStatus == OrderStatus.CANCELLED;

            case DELIVERED, CANCELLED ->
                    false;
        };

        if (!validTransition) {
            throw new InvalidOrderStatusException(
                    currentStatus,
                    newStatus
            );
        }
    }

    public OrderResponse assignDriver(
            UUID orderId,
            UUID driverId
    ) {

        log.info(
                "Assigning driver orderId={} driverId={}",
                orderId,
                driverId
        );

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        if (order.getStatus() != OrderStatus.CREATED) {

            log.warn(
                    "Driver assignment rejected orderId={} currentStatus={}",
                    orderId,
                    order.getStatus()
            );

            throw new DriverAssignmentException(
                    "A driver can only be assigned to an order with CREATED status"
            );
        }

        DriverClient.DriverResponse driver =
                driverClient.findById(driverId);

        if (!driver.active()) {

            log.warn(
                    "Driver assignment rejected orderId={} driverId={} reason=inactive_driver",
                    orderId,
                    driverId
            );

            throw new DriverAssignmentException(
                    "The driver is not active"
            );
        }

        order.setDriverId(driver.id());

        Order updatedOrder = orderRepository.save(order);

        log.info(
                "Driver assigned successfully orderId={} driverId={}",
                updatedOrder.getId(),
                driver.id()
        );

        return toResponse(updatedOrder);
    }

    private OrderResponse toResponse(Order order) {

        return new OrderResponse(
                order.getId(),
                order.getStatus(),
                order.getOrigin(),
                order.getDestination(),
                order.getCreatedAt(),
                order.getUpdatedAt(),
                order.getDriverId()
        );
    }
}