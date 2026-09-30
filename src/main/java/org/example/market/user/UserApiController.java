
/*
package org.example.market.user;

import org.example.market.annonce.AnnonceService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

        import java.util.HashMap;
import java.util.Map;

@RestController // ✅ Seulement @RestController, pas @Controller
@RequestMapping("/api/v1/users") // ✅ Route API dédiée
@CrossOrigin(origins = {"http://localhost:3000", "capacitor://localhost", "http://localhost"})
public class UserApiController {

    private final UserRepository userRepository;
    private final AnnonceService annonceService;

    public UserApiController(UserRepository userRepository, AnnonceService annonceService) {
        this.userRepository = userRepository;
        this.annonceService = annonceService;
    }

    // GET /api/v1/users/me (profil utilisateur connecté)
    @GetMapping("/me")
    public UserDTO getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        return UserDTO.fromEntity(user);
    }

    // PUT /api/v1/users/me (mettre à jour)
    @PutMapping("/me")
    public UserDTO updateCurrentUser(@RequestBody UserDTO userDto,
                                     @AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        // Mise à jour
        user.setNom(userDto.getNom());
        user.setPrenom(userDto.getPrenom());
        user.setEmail(userDto.getEmail());
        user.setTelephone(userDto.getTelephone());
        user.setAdresse(userDto.getAdresse());
        user.setVille(userDto.getVille());

        User updatedUser = userRepository.save(user);
        return UserDTO.fromEntity(updatedUser);
    }

    // GET /api/v1/users/me/stats
    @GetMapping("/me/stats")
    public Map<String, Object> getUserStats(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        int nombreAnnonces = annonceService.countByVendeurUsername(user.getUsername());

        Map<String, Object> stats = new HashMap<>();
        stats.put("userId", user.getId());
        stats.put("username", user.getUsername());
        stats.put("nombreAnnonces", nombreAnnonces);
        stats.put("email", user.getEmail());
        stats.put("telephone", user.getTelephone());
        stats.put("adresse", user.getAdresse());
        stats.put("ville", user.getVille());

        return stats;
    }

    // GET /api/v1/users/{id} (voir un utilisateur par ID - public)
    @GetMapping("/{id}")
    public UserDTO getUserById(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        return UserDTO.fromEntity(user);
    }
}
*/
