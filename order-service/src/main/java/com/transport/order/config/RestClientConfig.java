package com.transport.order.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean
    public RestClient driverRestClient(
            @Value("${services.driver.url}") String driverServiceUrl
    ) {
        return RestClient.builder()
                .baseUrl(driverServiceUrl)
                .build();
    }
}