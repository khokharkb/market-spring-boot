package org.example.market;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
// Votre package actuel
public class MarketApplication {

    public static void main(String[] args) {
        SpringApplication.run(MarketApplication.class, args);
        System.out.println("🚀 Spring Boot démarré sur http://localhost:8080");
        System.out.println("📱 API Mobile disponible sur /api/mobile");
        System.out.println("🔍 Scanning packages: org.example.market, com.market");
    }
}