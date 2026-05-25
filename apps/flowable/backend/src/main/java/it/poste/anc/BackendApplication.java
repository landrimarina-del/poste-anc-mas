package it.poste.anc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;

/**
 * Entry point del modular monolith Scrivania Digitale ANC.
 * Sprint 0: bootstrap, security (Basic Auth + RBAC su tabella app_user) e health.
 */
@SpringBootApplication
@EnableRetry
public class BackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }
}
