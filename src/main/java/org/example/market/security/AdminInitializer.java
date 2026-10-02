package org.example.market.security;

import lombok.extern.slf4j.Slf4j;
import org.example.market.user.User;
import org.example.market.user.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Crée le compte administrateur au démarrage s'il n'existe pas encore.
 * Le mot de passe vient de la variable d'environnement ADMIN_PASSWORD.
 */
@Slf4j
@Component
public class AdminInitializer implements CommandLineRunner {

    private static final String ADMIN_USERNAME = "admin";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminPassword;
    private final String adminEmail;

    public AdminInitializer(UserRepository userRepository,
                            PasswordEncoder passwordEncoder,
                            @Value("${ADMIN_PASSWORD}") String adminPassword,
                            @Value("${ADMIN_EMAIL:admin@market.local}") String adminEmail) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminPassword = adminPassword;
        this.adminEmail = adminEmail;
    }

    @Override
    public void run(String... args) {
        if (userRepository.findByUsername(ADMIN_USERNAME).isPresent()) {
            return;
        }

        User admin = new User();
        admin.setUsername(ADMIN_USERNAME);
        admin.setEmail(adminEmail);
        admin.setPassword(passwordEncoder.encode(adminPassword));
        admin.setNom("Admin");
        admin.setPrenom("Admin");
        admin.setActive(true);
        admin.setRole("ADMIN");

        userRepository.save(admin);
        log.info("Compte administrateur '{}' créé", ADMIN_USERNAME);
    }
}
