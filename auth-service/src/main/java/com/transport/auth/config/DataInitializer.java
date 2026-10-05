package com.transport.auth.config;

import com.transport.auth.entity.User;
import com.transport.auth.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initUsers(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        return args -> {

            if (!userRepository.existsByUsername("admin")) {

                User admin = new User(
                        null,
                        "admin",
                        passwordEncoder.encode("admin123"),
                        "ADMIN",
                        true
                );

                userRepository.save(admin);
            }
        };
    }
}
