/*package org.example.market.annonce;

import org.example.market.user.User;
import org.example.market.user.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/annonces")
@CrossOrigin(origins = {"http://localhost:3000", "capacitor://localhost", "http://localhost"})
public class AnnonceApiController {

    private final AnnonceService annonceService;
    private final UserRepository userRepository; // Pour récupérer l'utilisateur

    public AnnonceApiController(AnnonceService annonceService,
                                UserRepository userRepository) {
        this.annonceService = annonceService;
        this.userRepository = userRepository;
    }

    // ==================== ENDPOINTS PUBLICS ====================

    // GET /api/v1/annonces - Liste toutes les annonces
    @GetMapping
    public List<AnnonceDto> getAllAnnonces(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String categorie) {

        List<Annonce> annonces = annonceService.searchAnnonces(search, categorie);
        return annonces.stream()
                .map(AnnonceDto::fromEntity)
                .collect(Collectors.toList());
    }

    // GET /api/v1/annonces/{id} - Détail d'une annonce (incrémente les vues)
    @GetMapping("/{id}")
    public AnnonceDto getAnnonceById(@PathVariable Long id) {
        Annonce annonce = annonceService.findByIdAndIncrementViews(id);
        return AnnonceDto.fromEntity(annonce);
    }

    // GET /api/v1/annonces/categories - Liste des catégories
    @GetMapping("/categories")
    public List<String> getAllCategories() {
        return annonceService.findAllCategories();
    }

    // GET /api/v1/annonces/vendeur/{username} - Annonces d'un vendeur
    @GetMapping("/vendeur/{username}")
    public List<AnnonceDto> getAnnoncesByVendeur(@PathVariable String username) {
        List<Annonce> annonces = annonceService.findByVendeurUsername(username);
        return annonces.stream()
                .map(AnnonceDto::fromEntity)
                .collect(Collectors.toList());
    }

    // GET /api/v1/annonces/stats - Statistiques globales
    @GetMapping("/stats")
    public Map<String, Object> getGlobalStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalAnnonces", annonceService.countAll());
        stats.put("vendeursDistincts", annonceService.countVendeursDistinct());

        // Stats par catégorie
        Map<String, Long> statsByCategory = new HashMap<>();
        List<String> categories = annonceService.findAllCategories();
        for (String category : categories) {
            statsByCategory.put(category, annonceService.countAnnoncesByCategorie(category));
        }
        stats.put("statsByCategory", statsByCategory);

        return stats;
    }

    // ==================== ENDPOINTS PRIVÉS ====================

    // POST /api/v1/annonces - Créer une annonce (utilisateur connecté)
    @PostMapping
    public AnnonceDto createAnnonce(
            @RequestBody CreateAnnonceRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        User vendeur = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        Annonce annonce = new Annonce();
        annonce.setTitre(request.getTitre());
        annonce.setDescription(request.getDescription());
        annonce.setPrix(request.getPrix());
        annonce.setCategorie(request.getCategorie());
        annonce.setTelephone(request.getTelephone());
        annonce.setImagePath(request.getImagePath()); // À gérer : upload image
        annonce.setVendeur(vendeur);

        Annonce savedAnnonce = annonceService.save(annonce);
        return AnnonceDto.fromEntity(savedAnnonce);
    }

    // PUT /api/v1/annonces/{id} - Modifier une annonce (propriétaire seulement)
    @PutMapping("/{id}")
    public AnnonceDto updateAnnonce(
            @PathVariable Long id,
            @RequestBody UpdateAnnonceRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        // Vérifier que l'utilisateur est propriétaire
        if (!annonceService.isOwner(id, userDetails.getUsername())) {
            throw new RuntimeException("Vous n'êtes pas autorisé à modifier cette annonce");
        }

        // Récupérer l'annonce existante
        Annonce existingAnnonce = annonceService.findById(id);

        // Mettre à jour les champs
        if (request.getTitre() != null) {
            existingAnnonce.setTitre(request.getTitre());
        }
        if (request.getDescription() != null) {
            existingAnnonce.setDescription(request.getDescription());
        }
        if (request.getPrix() != null) {
            existingAnnonce.setPrix(request.getPrix());
        }
        if (request.getCategorie() != null) {
            existingAnnonce.setCategorie(request.getCategorie());
        }
        if (request.getTelephone() != null) {
            existingAnnonce.setTelephone(request.getTelephone());
        }
        if (request.getImagePath() != null) {
            existingAnnonce.setImagePath(request.getImagePath());
        }

        Annonce updatedAnnonce = annonceService.save(existingAnnonce);
        return AnnonceDto.fromEntity(updatedAnnonce);
    }

    // DELETE /api/v1/annonces/{id} - Supprimer une annonce
    @DeleteMapping("/{id}")
    public Map<String, String> deleteAnnonce(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        // Vérifier que l'utilisateur a le droit de supprimer
        if (!annonceService.canDelete(id)) {
            throw new RuntimeException("Vous n'êtes pas autorisé à supprimer cette annonce");
        }

        annonceService.delete(id);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Annonce supprimée avec succès");
        response.put("annonceId", id.toString());

        return response;
    }

    // ==================== DTOs REQUEST ====================

    // Classe pour la création d'annonce
    public static class CreateAnnonceRequest {
        private String titre;
        private String description;
        private Double prix;
        private String categorie;
        private String telephone;
        private String imagePath; // Base64 ou URL

        // Getters et Setters
        public String getTitre() { return titre; }
        public void setTitre(String titre) { this.titre = titre; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public Double getPrix() { return prix; }
        public void setPrix(Double prix) { this.prix = prix; }

        public String getCategorie() { return categorie; }
        public void setCategorie(String categorie) { this.categorie = categorie; }

        public String getTelephone() { return telephone; }
        public void setTelephone(String telephone) { this.telephone = telephone; }

        public String getImagePath() { return imagePath; }
        public void setImagePath(String imagePath) { this.imagePath = imagePath; }
    }

    // Classe pour la mise à jour d'annonce
    public static class UpdateAnnonceRequest {
        private String titre;
        private String description;
        private Double prix;
        private String categorie;
        private String telephone;
        private String imagePath;

        // Getters et Setters
        public String getTitre() { return titre; }
        public void setTitre(String titre) { this.titre = titre; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public Double getPrix() { return prix; }
        public void setPrix(Double prix) { this.prix = prix; }

        public String getCategorie() { return categorie; }
        public void setCategorie(String categorie) { this.categorie = categorie; }

        public String getTelephone() { return telephone; }
        public void setTelephone(String telephone) { this.telephone = telephone; }

        public String getImagePath() { return imagePath; }
        public void setImagePath(String imagePath) { this.imagePath = imagePath; }
    }
}
*/