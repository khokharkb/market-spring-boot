/*package org.example.market.security;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.market.user.User;
import org.example.market.user.UserDTO;
import org.example.market.user.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@CrossOrigin(
        origins = {"http://localhost:3000", "capacitor://localhost", "http://localhost", "http://10.0.2.2:8080"},
        allowCredentials = "true"
)
public class MobileAuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;

    public MobileAuthController(AuthenticationManager authenticationManager,
                                UserRepository userRepository) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
    }

    // Login pour mobile
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> mobileLogin(
            @RequestBody LoginRequest loginRequest,
            HttpServletRequest request,
            HttpServletResponse response) {

        try {
            // Authentifier l'utilisateur
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );

            // Définir l'authentification dans le contexte de sécurité
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Créer la session
            request.getSession().setAttribute(
                    HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                    SecurityContextHolder.getContext()
            );

            // Récupérer l'utilisateur
            User user = userRepository.findByUsername(loginRequest.getUsername())
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

            // Retourner la réponse
            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("success", true);
            responseBody.put("message", "Connexion réussie");
            responseBody.put("user", UserDTO.fromEntity(user));
            responseBody.put("sessionId", request.getSession().getId());

            return ResponseEntity.ok(responseBody);

        } catch (Exception e) {
            Map<String, Object> errorBody = new HashMap<>();
            errorBody.put("success", false);
            errorBody.put("message", "Identifiants incorrects");
            return ResponseEntity.status(401).body(errorBody);
        }
    }

    // Vérifier si l'utilisateur est connecté
    @GetMapping("/check")
    public ResponseEntity<Map<String, Object>> checkAuth(@AuthenticationPrincipal UserDetails userDetails) {
        Map<String, Object> response = new HashMap<>();

        if (userDetails != null) {
            User user = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

            response.put("authenticated", true);
            response.put("user", UserDTO.fromEntity(user));
        } else {
            response.put("authenticated", false);
            response.put("user", null);
        }

        return ResponseEntity.ok(response);
    }

    // Logout pour mobile
    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> mobileLogout(HttpServletRequest request) {
        request.getSession().invalidate();
        SecurityContextHolder.clearContext();

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Déconnexion réussie");

        return ResponseEntity.ok(response);
    }

    // Classe pour la requête de login
    public static class LoginRequest {
        private String username;
        private String password;

        // Getters et setters
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
}
*/