package com.transport.order.client;

import com.transport.order.exception.DriverAssignmentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.UUID;

@Component
public class DriverClient {

    private static final Logger log =
            LoggerFactory.getLogger(DriverClient.class);

    private final RestClient restClient;

    public DriverClient(RestClient driverRestClient) {
        this.restClient = driverRestClient;
    }

    public DriverResponse findById(UUID driverId) {

        log.info(
                "Requesting driver from driver-service driverId={}",
                driverId
        );

        try {

            DriverResponse driver = restClient
                    .get()
                    .uri("/api/drivers/{id}", driverId)
                    .retrieve()
                    .body(DriverResponse.class);

            log.info(
                    "Driver retrieved successfully driverId={} active={}",
                    driverId,
                    driver != null && driver.active()
            );

            return driver;

        } catch (HttpClientErrorException.NotFound e) {

            log.warn(
                    "Driver not found driverId={}",
                    driverId
            );

            throw new DriverAssignmentException(
                    "Driver with id " + driverId + " was not found"
            );

        } catch (RestClientException e) {

            log.error(
                    "Could not communicate with driver-service driverId={}",
                    driverId,
                    e
            );

            throw new DriverAssignmentException(
                    "Could not communicate with driver-service"
            );
        }
    }

    public record DriverResponse(
            UUID id,
            String name,
            String licenseNumber,
            boolean active
    ) {
    }
}