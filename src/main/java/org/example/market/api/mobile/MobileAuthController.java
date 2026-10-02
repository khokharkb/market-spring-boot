package org.example.market.api.mobile;

import lombok.extern.slf4j.Slf4j;
import org.example.market.user.User;
import org.example.market.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

/**
 * API d'authentification utilisée par l'application mobile (Capacitor).
 */
@Slf4j
@RestController
@RequestMapping("/api/mobile")
@CrossOrigin(origins = "*")
public class MobileAuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public MobileAuthController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/login")
    public ResponseEntity<?> mobileLogin(@RequestBody Map<String, String> request) {
        // Le champ "email" accepte aussi un nom d'utilisateur
        String emailOrUsername = request.get("email");
        String password = request.get("password");

        if (emailOrUsername == null || password == null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "Email/username et mot de passe requis"
            ));
        }

        try {
            Optional<User> user = userRepository.findByEmail(emailOrUsername)
                    .or(() -> userRepository.findByUsername(emailOrUsername));

            // Même message d'erreur que l'utilisateur existe ou non,
            // pour ne pas révéler quels comptes existent
            if (user.isEmpty() || !passwordEncoder.matches(password, user.get().getPassword())) {
                log.info("Échec de connexion mobile pour : {}", emailOrUsername);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                        "success", false,
                        "error", "Email/username ou mot de passe incorrect"
                ));
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Connexion réussie");
            response.put("user", createUserResponse(user.get()));
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Erreur lors de la connexion mobile", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "error", "Erreur serveur"
            ));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> mobileRegister(@RequestBody Map<String, String> request) {
        String name = request.get("name");
        String email = request.get("email");
        String password = request.get("password");

        if (name == null || email == null || password == null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "Tous les champs sont requis"
            ));
        }

        try {
            if (userRepository.findByEmail(email).isPresent()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "error", "Cet email est déjà utilisé"
                ));
            }

            // Sur mobile, le nom saisi sert de nom d'utilisateur
            if (userRepository.findByUsername(name).isPresent()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "error", "Ce nom d'utilisateur est déjà utilisé"
                ));
            }

            User newUser = new User();
            newUser.setUsername(name);
            newUser.setEmail(email);
            newUser.setPassword(passwordEncoder.encode(password));
            newUser.setDateInscription(LocalDateTime.now());
            newUser.setActive(true);
            newUser.setRole("USER");

            User savedUser = userRepository.save(newUser);
            log.info("Nouvel utilisateur inscrit depuis le mobile (id {})", savedUser.getId());

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Inscription réussie",
                    "user", createUserResponse(savedUser)
            ));

        } catch (Exception e) {
            log.error("Erreur lors de l'inscription mobile", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "error", "Erreur serveur"
            ));
        }
    }

    /** Vérifie que l'API répond (utilisé par l'application mobile au démarrage). */
    @GetMapping("/test")
    public ResponseEntity<?> test() {
        return ResponseEntity.ok(Map.of(
                "status", "API Mobile active",
                "timestamp", new Date()
        ));
    }

    @GetMapping("/health")
    public String health() {
        return "OK";
    }

    /** Données de l'utilisateur renvoyées au mobile (jamais le mot de passe). */
    private Map<String, Object> createUserResponse(User user) {
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("id", user.getId());
        userMap.put("name", user.getUsername());
        userMap.put("email", user.getEmail());
        userMap.put("username", user.getUsername());
        userMap.put("nom", user.getNom());
        userMap.put("prenom", user.getPrenom());
        userMap.put("role", user.getRole());
        return userMap;
    }
}
