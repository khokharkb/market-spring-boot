package org.example.market.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.example.market.user.User;
import org.example.market.user.UserRepository;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Après une connexion Google réussie : retrouve ou crée le compte Market
 * correspondant à l'email, puis connecte l'utilisateur comme une connexion normale.
 */
@Slf4j
@Component
public class GoogleLoginSuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserDetailsService userDetailsService;
    private final SecurityContextRepository securityContextRepository =
            new HttpSessionSecurityContextRepository();

    public GoogleLoginSuccessHandler(UserRepository userRepository,
                                     PasswordEncoder passwordEncoder,
                                     UserDetailsService userDetailsService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userDetailsService = userDetailsService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        // Informations envoyées par Google
        OidcUser googleUser = (OidcUser) authentication.getPrincipal();
        String email = googleUser.getEmail();
        Boolean emailVerified = googleUser.getEmailVerified();
        String firstName = googleUser.getGivenName();
        String lastName = googleUser.getFamilyName();

        // TODO 1 : on ne fait confiance qu'à un email vérifié par Google
        if (email == null || !Boolean.TRUE.equals(emailVerified)) {
            log.warn("Connexion Google refusée : email absent ou non vérifié");
            response.sendRedirect("/login?error");
            return;
        }

        // TODO 2 : même email = même personne ; sinon on crée le compte
        User user = userRepository.findByEmail(email)
                .orElseGet(() -> createGoogleUser(email, firstName, lastName));

        // TODO 3 : un compte désactivé par l'admin ne peut pas passer par Google
        if (!user.isActive()) {
            log.warn("Connexion Google refusée : compte désactivé ({})", user.getUsername());
            response.sendRedirect("/login?error");
            return;
        }

        // On remplace l'identité Google par une connexion Market classique
        UserDetails details = userDetailsService.loadUserByUsername(user.getUsername());
        Authentication marketAuth = new UsernamePasswordAuthenticationToken(
                details, null, details.getAuthorities());

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(marketAuth);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, request, response);

        response.sendRedirect("/");
    }

    // TODO 4 : création d'un compte Market pour un nouvel utilisateur Google
    private User createGoogleUser(String email, String firstName, String lastName) {
        User user = new User();
        user.setUsername(generateUniqueUsername(email));
        user.setEmail(email);
        // Mot de passe aléatoire que personne ne connaît : la connexion se fait via Google
        user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
        user.setNom(lastName);
        user.setPrenom(firstName);
        user.setDateInscription(LocalDateTime.now());
        user.setRole("USER");
        user.setActive(true);

        User saved = userRepository.save(user);
        log.info("Nouveau compte créé via Google : {}", saved.getUsername());
        return saved;
    }

    // TODO 5 : nom d'utilisateur unique à partir de l'email
    private String generateUniqueUsername(String email) {
        String base = email.substring(0, email.indexOf('@'))
                .replaceAll("[^a-zA-Z0-9]", "");

        if (base.length() < 3) {
            base = "user" + base;
        }
        if (base.length() > 40) {
            base = base.substring(0, 40); // garde de la place pour un numéro (max 50)
        }

        String candidate = base;
        int counter = 1;
        while (userRepository.findByUsername(candidate).isPresent()) {
            candidate = base + counter;
            counter++;
        }
        return candidate;
    }
}