package org.example.market.api.mobile;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.example.market.user.User;
import org.example.market.user.UserRepository;
import java.util.*;

@RestController
@RequestMapping("/api/mobile")
@CrossOrigin(origins = "*")
public class MobileAuthController {

    @Autowired
    private UserRepository userRepository;

    // Vérifiez que userRepository est injecté
    public MobileAuthController() {
        System.out.println("✅ MobileAuthController créé");
    }

    @PostMapping("/login")
    public ResponseEntity<?> mobileLogin(@RequestBody Map<String, String> request) {
        try {
            String emailOrUsername = request.get("email"); // Peut être email OU username
            String password = request.get("password");

            System.out.println("📱 Tentative de login: " + emailOrUsername);

            if (emailOrUsername == null || password == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "error", "Email/username et mot de passe requis"
                ));
            }

            // Chercher l'utilisateur par EMAIL d'abord
            Optional<User> userByEmail = userRepository.findByEmail(emailOrUsername);
            User user = null;

            if (userByEmail.isPresent()) {
                user = userByEmail.get();
                System.out.println("✅ Utilisateur trouvé par email: " + user.getEmail());
            } else {
                // Si pas trouvé par email, chercher par USERNAME
                Optional<User> userByUsername = userRepository.findByUsername(emailOrUsername);
                if (userByUsername.isPresent()) {
                    user = userByUsername.get();
                    System.out.println("✅ Utilisateur trouvé par username: " + user.getUsername());
                }
            }

            if (user != null) {
                // Vérifier le mot de passe (EN CLAIR pour l'instant - à changer plus tard)
                if (user.getPassword().equals(password)) {
                    // Créer la réponse JSON
                    Map<String, Object> response = new HashMap<>();
                    response.put("success", true);
                    response.put("message", "Connexion réussie");
                    response.put("user", createUserResponse(user));

                    return ResponseEntity.ok(response);
                } else {
                    System.out.println("❌ Mot de passe incorrect pour: " + emailOrUsername);
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .body(Map.of(
                                    "success", false,
                                    "error", "Mot de passe incorrect"
                            ));
                }
            } else {
                System.out.println("❌ Utilisateur non trouvé: " + emailOrUsername);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of(
                                "success", false,
                                "error", "Email/username ou mot de passe incorrect"
                        ));
            }
        } catch (Exception e) {
            System.out.println("❌ Erreur login: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "success", false,
                            "error", "Erreur serveur: " + e.getMessage()
                    ));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> mobileRegister(@RequestBody Map<String, String> request) {
        try {
            String name = request.get("name");
            String email = request.get("email");
            String password = request.get("password");

            System.out.println("📱 Tentative d'inscription: " + email);

            if (name == null || email == null || password == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "error", "Tous les champs sont requis"
                ));
            }

            // Vérifier si l'email existe déjà
            if (userRepository.findByEmail(email).isPresent()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "error", "Cet email est déjà utilisé"
                ));
            }

            // Vérifier si le username existe déjà (utilisation du nom comme username)
            if (userRepository.findByUsername(name).isPresent()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "error", "Ce nom d'utilisateur est déjà utilisé"
                ));
            }

            // Créer un nouvel utilisateur
            User newUser = new User();
            newUser.setUsername(name);  // Utiliser le "name" comme username
            newUser.setEmail(email);
            newUser.setPassword(password); // ⚠️ À crypter plus tard !
            newUser.setDateInscription(java.time.LocalDateTime.now());
            newUser.setActive(true);
            newUser.setRole("USER");

            // Sauvegarder dans la base de données
            User savedUser = userRepository.save(newUser);

            System.out.println("✅ Utilisateur enregistré avec ID: " + savedUser.getId());

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Inscription réussie",
                    "user", createUserResponse(savedUser)
            ));

        } catch (Exception e) {
            System.out.println("❌ Erreur inscription: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "success", false,
                            "error", "Erreur serveur: " + e.getMessage()
                    ));
        }
    }

    @GetMapping("/test")
    public ResponseEntity<?> test() {
        try {
            long userCount = userRepository.count();
            System.out.println("📱 Test API mobile appelé - Utilisateurs dans DB: " + userCount);

            // Récupérer tous les utilisateurs pour debug
            List<User> allUsers = userRepository.findAll();
            List<Map<String, Object>> usersList = new ArrayList<>();

            for (User user : allUsers) {
                usersList.add(Map.of(
                        "id", user.getId(),
                        "username", user.getUsername(),
                        "email", user.getEmail()
                ));
            }

            return ResponseEntity.ok(Map.of(
                    "status", "API Mobile active",
                    "usersCount", userCount,
                    "users", usersList,
                    "timestamp", new Date(),
                    "message", "Backend Spring Boot accessible avec MySQL"
            ));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of(
                    "status", "ERROR",
                    "error", e.getMessage(),
                    "message", "Problème avec la base de données"
            ));
        }
    }

    @GetMapping("/health")
    public String health() {
        return "OK";
    }

    @GetMapping("/debug-users")
    public ResponseEntity<?> debugUsers() {
        try {
            List<User> allUsers = userRepository.findAll();
            List<Map<String, Object>> users = new ArrayList<>();

            for (User user : allUsers) {
                Map<String, Object> userMap = new HashMap<>();
                userMap.put("id", user.getId());
                userMap.put("username", user.getUsername());
                userMap.put("email", user.getEmail());
                userMap.put("password", user.getPassword());
                userMap.put("active", user.isActive());
                userMap.put("role", user.getRole());
                users.add(userMap);
            }

            return ResponseEntity.ok(Map.of(
                    "total", users.size(),
                    "users", users,
                    "database", "MySQL market"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "error", e.getMessage(),
                            "stacktrace", Arrays.toString(e.getStackTrace())
                    ));
        }
    }

    // Méthode utilitaire pour créer la réponse utilisateur
    private Map<String, Object> createUserResponse(User user) {
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("id", user.getId());
        userMap.put("name", user.getUsername());  // Envoyer username comme "name"
        userMap.put("email", user.getEmail());
        userMap.put("username", user.getUsername());
        userMap.put("nom", user.getNom());
        userMap.put("prenom", user.getPrenom());
        userMap.put("role", user.getRole());
        return userMap;
    }
}