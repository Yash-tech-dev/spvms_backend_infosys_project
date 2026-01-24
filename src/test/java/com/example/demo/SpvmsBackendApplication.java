package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main Entry Point for the SPVMS (Smart Procurement & Vendor Management System) Backend.
 * This class initializes the Spring Boot framework, starts the embedded Tomcat server,
 * and triggers the component scanning process across the project.
 */
@SpringBootApplication
public class SpvmsBackendApplication {

    /**
     * The main method that serves as the starting point for the Java Virtual Machine (JVM).
     * Working:
     * 1. Launches the Spring Application Context.
     * 2. Performs Auto-configuration based on the 'application.properties' file.
     * 3. Initializes the JPA/Hibernate layers and database connections.
     * @param args command-line arguments passed during startup.
     */
    public static void main(String[] args) {
        SpringApplication.run(SpvmsBackendApplication.class, args);
    }

    // 1. Define the PasswordEncoder bean for AuthController

}