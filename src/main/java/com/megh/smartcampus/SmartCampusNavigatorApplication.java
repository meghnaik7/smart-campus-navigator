package com.megh.smartcampus;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class SmartCampusNavigatorApplication {
    public static void main(String[] args) {
        SpringApplication.run(SmartCampusNavigatorApplication.class, args);
        System.out.println("\n========================================");
        System.out.println("  Smart Campus Navigator - STARTED");
        System.out.println("  API:     http://localhost:8080");
        System.out.println("  Swagger: http://localhost:8080/swagger-ui.html");
        System.out.println("  Health:  http://localhost:8080/actuator/health");
        System.out.println("========================================\n");
    }
}
