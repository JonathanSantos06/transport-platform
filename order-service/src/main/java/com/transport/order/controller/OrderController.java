package com.transport.order.controller;

import com.transport.order.dto.AssignDriverRequest;
import com.transport.order.dto.OrderCreateRequest;
import com.transport.order.dto.OrderResponse;
import com.transport.order.dto.OrderStatusUpdateRequest;
import com.transport.order.entity.OrderFile;
import com.transport.order.entity.OrderStatus;
import com.transport.order.service.OrderFileService;
import com.transport.order.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    private final OrderFileService orderFileService;

    public OrderController(OrderService orderService, OrderFileService orderFileService) {
        this.orderService = orderService;
        this.orderFileService = orderFileService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse create(
            @Valid @RequestBody OrderCreateRequest request
    ) {
        return orderService.create(request);
    }

    @PostMapping("/{id}/driver")
    public OrderResponse assignDriver(
            @PathVariable UUID id,
            @Valid @RequestBody AssignDriverRequest request
    ) {
        return orderService.assignDriver(
                id,
                request.driverId()
        );
    }

    @PostMapping(
            value = "/{id}/files",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @ResponseStatus(HttpStatus.CREATED)
    public OrderFile uploadFile(
            @PathVariable UUID id,
            @RequestParam("file") MultipartFile file
    ) {
        return orderFileService.upload(id, file);
    }

    @GetMapping
    public List<OrderResponse> findAll(
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) String origin,
            @RequestParam(required = false) String destination,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime to
    ) {
        return orderService.findAll(
                status,
                origin,
                destination,
                from,
                to
        );
    }

    @GetMapping("/{id}")
    public OrderResponse findById(
            @PathVariable UUID id
    ) {
        return orderService.findById(id);
    }

    @PatchMapping("/{id}/status")
    public OrderResponse updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody OrderStatusUpdateRequest request
    ) {
        return orderService.updateStatus(
                id,
                request.status()
        );
    }
}