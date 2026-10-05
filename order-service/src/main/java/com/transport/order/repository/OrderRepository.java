package com.transport.order.repository;

import com.transport.order.entity.Order;
import com.transport.order.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface OrderRepository
        extends JpaRepository<Order, UUID>,
        JpaSpecificationExecutor<Order> {
}