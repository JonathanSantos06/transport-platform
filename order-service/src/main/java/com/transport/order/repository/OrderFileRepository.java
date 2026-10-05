package com.transport.order.repository;

import com.transport.order.entity.OrderFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OrderFileRepository extends JpaRepository<OrderFile, UUID> {

    List<OrderFile> findAllByOrderId(UUID orderId);
}