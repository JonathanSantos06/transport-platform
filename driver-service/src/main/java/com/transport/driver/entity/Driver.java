package com.transport.driver.entity;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(
        name = "drivers",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_driver_license_number",
                        columnNames = "license_number"
                )
        }
)
public class Driver {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(name = "license_number", nullable = false)
    private String licenseNumber;

    @Column(nullable = false)
    private boolean active;

    public Driver() {
    }

    public Driver(
            UUID id,
            String name,
            String licenseNumber,
            boolean active
    ) {
        this.id = id;
        this.name = name;
        this.licenseNumber = licenseNumber;
        this.active = active;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getLicenseNumber() {
        return licenseNumber;
    }

    public boolean isActive() {
        return active;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLicenseNumber(String licenseNumber) {
        this.licenseNumber = licenseNumber;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}