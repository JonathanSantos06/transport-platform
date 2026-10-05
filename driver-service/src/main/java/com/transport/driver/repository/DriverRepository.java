package com.transport.driver.repository;

import com.transport.driver.entity.Driver;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DriverRepository extends JpaRepository<Driver, UUID> {

    List<Driver> findAllByActiveTrue();

    boolean existsByLicenseNumber(String licenseNumber);
}