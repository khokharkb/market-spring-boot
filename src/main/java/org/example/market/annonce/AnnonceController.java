package org.example.market.annonce;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.example.market.favoris.FavoriService;
import org.example.market.message.MessageService;
import org.example.market.panier.PanierService;
import org.example.market.rating.Rating;
import org.example.market.rating.RatingRepository;
import org.example.market.user.UserRepository;
import org.example.market.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@CrossOrigin(origins = {"http://localhost:3000", "capacitor://localhost", "http://localhost"})
public class AnnonceController {

    private final AnnonceService annonceService;
    private final PanierService panierService; // Ajouter ceci
    private final AnnonceRepository annonceRepository;
    private final UserRepository userRepository;
    private final Path rootLocation = Paths.get("uploads");
    private final String UPLOAD_DIR = "uploads/";
    private final MessageService messageService;
    private final FavoriService favoriService;
    private final RatingRepository ratingRepository;

    public AnnonceController(AnnonceService annonceService,
                             UserRepository userRepository,
                             AnnonceRepository annonceRepository,
                             MessageService messageService,PanierService panierService,FavoriService favoriService,RatingRepository ratingRepository) {
        this.annonceService = annonceService;
        this.userRepository = userRepository;
        this.annonceRepository = annonceRepository;
        this.messageService = messageService;
        this.panierService = panierService;
        this.favoriService = favoriService;
        this.ratingRepository = ratingRepository;

        try {
            Files.createDirectories(rootLocation);
            System.out.println("✅ Dossier uploads créé: " + rootLocation.toAbsolutePath());
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize storage!", e);
        }
    }

    // === AFFICHER LA LISTE DES ANNONCES ===
    // === AFFICHER LA LISTE DES ANNONCES ===
    @GetMapping("/annonces")
    public String getAnnonces(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String category,
            Model model, @AuthenticationPrincipal UserDetails userDetails) {
        List<Annonce> annonces = annonceService.findAll();
        model.addAttribute("annonces", annonces);


        // 2. Filtrer par recherche si paramètre existe
        if (search != null && !search.trim().isEmpty()) {
            final String searchLower = search.toLowerCase();
            annonces = annonces.stream()
                    .filter(a ->
                            (a.getTitre() != null && a.getTitre().toLowerCase().contains(searchLower)) ||
                                    (a.getDescription() != null && a.getDescription().toLowerCase().contains(searchLower))
                    )
                    .collect(Collectors.toList());
        }

        // 3. Filtrer par catégorie si paramètre existe
        if (category != null && !category.trim().isEmpty()) {
            annonces = annonces.stream()
                    .filter(a -> category.equals(a.getCategorie()))
                    .collect(Collectors.toList());
        }

        model.addAttribute("annonces", annonces);

        // 4. Ajouter les paramètres pour pré-remplir le formulaire
        model.addAttribute("search", search);
        model.addAttribute("category", category);


        // Ajouter l'username courant pour vérifier la propriété dans le template
        if (userDetails != null) {
            String username = userDetails.getUsername();
            model.addAttribute("currentUsername", username);

            // Vérifier si l'utilisateur est admin
            boolean isAdmin = userDetails.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            model.addAttribute("isAdmin", isAdmin);

            // Récupérer l'utilisateur complet depuis la base de données
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Récupérer les IDs des annonces favorites de cet utilisateur
            List<Long> favorisIds = favoriService.getFavorisByUser(user).stream()
                    .map(favori -> favori.getAnnonce().getId())
                    .collect(Collectors.toList());

            model.addAttribute("favorisIds", favorisIds);
        } else {
            // Si l'utilisateur n'est pas connecté, passer une liste vide
            model.addAttribute("favorisIds", new ArrayList<Long>());
        }

        // Statistiques supplémentaires (optionnel)
        long totalAnnonces = annonceService.countAll();
        long vendeursCount = annonceService.countVendeursDistinct();
        List<String> categories = annonceService.findAllCategories();

        model.addAttribute("totalAnnonces", totalAnnonces);
        model.addAttribute("vendeursCount", vendeursCount);
        model.addAttribute("categoriesCount", categories);

        return "annonces";
    }
    // === FORMULAIRE POUR AJOUTER UNE ANNONCE ===
    @GetMapping("/annonces/new")
    public String showCreateForm(Model model) {
        model.addAttribute("annonce", new Annonce());
        return "annonce-form";
    }

    // === SAUVEGARDER UNE NOUVELLE ANNONCE ===
    @PostMapping("/annonces")
    public String saveAnnonce(@ModelAttribute Annonce annonce,
                              @RequestParam("imageFile") MultipartFile imageFile,
                              @AuthenticationPrincipal UserDetails userDetails) {

        User vendeur = userRepository
                .findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        annonce.setVendeur(vendeur);

        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                String originalFilename = imageFile.getOriginalFilename();
                String fileExtension = "";
                if (originalFilename != null && originalFilename.contains(".")) {
                    fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
                }
                String filename = System.currentTimeMillis() + "_" + UUID.randomUUID() + fileExtension;

                Path destinationFile = this.rootLocation.resolve(Paths.get(filename))
                        .normalize().toAbsolutePath();

                Files.copy(imageFile.getInputStream(), destinationFile, StandardCopyOption.REPLACE_EXISTING);
                annonce.setImagePath(filename);

            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException("Échec de la sauvegarde de l'image: " + e.getMessage());
            }
        } else {
            annonce.setImagePath("default.jpg");
        }

        annonceService.save(annonce);
        return "redirect:/annonces";
    }

    // === FORMULAIRE DE MODIFICATION ===
    @GetMapping("/annonces/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model,
                               @AuthenticationPrincipal UserDetails userDetails,
                               RedirectAttributes redirectAttributes) {

        // Vérifier que l'utilisateur est le propriétaire (admin NE peut PAS modifier les annonces des autres)
        if (!annonceService.isOwner(id, userDetails.getUsername())) {
            redirectAttributes.addFlashAttribute("error", "❌ Vous n'êtes pas autorisé à modifier cette annonce");
            return "redirect:/annonces";
        }

        Annonce annonce = annonceService.findById(id);
        model.addAttribute("annonce", annonce);
        model.addAttribute("isEdit", true);

        return "annonce-form";
    }

    // === METTRE À JOUR UNE ANNONCE ===
    @PostMapping("/annonces/{id}/edit")
    public String updateAnnonce(@PathVariable Long id,
                                @ModelAttribute Annonce annonceDetails,
                                @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                                @AuthenticationPrincipal UserDetails userDetails,
                                RedirectAttributes redirectAttributes) {

        // Vérifier que l'utilisateur est le propriétaire (admin NE peut PAS modifier les annonces des autres)
        if (!annonceService.isOwner(id, userDetails.getUsername())) {
            redirectAttributes.addFlashAttribute("error", "❌ Vous n'êtes pas autorisé à modifier cette annonce");
            return "redirect:/annonces";
        }

        Annonce existingAnnonce = annonceService.findById(id);

        // Gérer la nouvelle image si fournie
        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                String originalFilename = imageFile.getOriginalFilename();
                String fileExtension = "";
                if (originalFilename != null && originalFilename.contains(".")) {
                    fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
                }
                String filename = System.currentTimeMillis() + "_" + UUID.randomUUID() + fileExtension;

                Path destinationFile = this.rootLocation.resolve(Paths.get(filename))
                        .normalize().toAbsolutePath();

                Files.copy(imageFile.getInputStream(), destinationFile, StandardCopyOption.REPLACE_EXISTING);
                annonceDetails.setImagePath(filename);

            } catch (IOException e) {
                e.printStackTrace();
                redirectAttributes.addFlashAttribute("error", "Erreur lors du upload de l'image");
                return "redirect:/annonces/" + id + "/edit";
            }
        } else {
            // Garder l'image existante
            annonceDetails.setImagePath(existingAnnonce.getImagePath());
        }

        // Mettre à jour l'annonce
        annonceService.update(id, annonceDetails);
        redirectAttributes.addFlashAttribute("success", "✅ Annonce modifiée avec succès");

        return "redirect:/annonces";
    }

    // === SUPPRIMER UNE ANNONCE ===
    @PostMapping("/annonces/{id}/delete")
    public String deleteAnnonce(@PathVariable Long id,
                                @AuthenticationPrincipal UserDetails userDetails,
                                RedirectAttributes redirectAttributes) {

        // Vérifier que l'utilisateur a le droit de supprimer (propriétaire OU admin)
        if (!annonceService.canDelete(id)) {
            redirectAttributes.addFlashAttribute("error", "❌ Vous n'êtes pas autorisé à supprimer cette annonce");
            return "redirect:/annonces";
        }

        try {
            annonceService.delete(id);
            redirectAttributes.addFlashAttribute("success", "🗑️ Annonce supprimée avec succès");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "❌ Erreur lors de la suppression: " + e.getMessage());
        }

        return "redirect:/annonces";
    }


    // SERVIR LES IMAGES
    @GetMapping("/uploads/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {
        try {
            Path file = rootLocation.resolve(filename);
            Resource resource = new UrlResource(file.toUri());

            if (resource.exists() || resource.isReadable()) {
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_TYPE, "image/jpeg")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (MalformedURLException e) {
            return ResponseEntity.notFound().build();
        }
    }
    // === MES ANNONCES (annonces de l'utilisateur connecté) ===
    @GetMapping("/mesannonces")
    public String mesAnnonces(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return "redirect:/login";
        }

        String username = userDetails.getUsername();
        List<Annonce> mesAnnonces = annonceService.findByVendeurUsername(username);

        // Récupérer l'utilisateur complet depuis la base de données
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Récupérer les IDs des annonces favorites de cet utilisateur
        List<Long> favorisIds = favoriService.getFavorisByUser(user).stream()
                .map(favori -> favori.getAnnonce().getId())
                .collect(Collectors.toList());

        // ✅ AJOUTEZ CES ATTRIBUTS MANQUANTS :
        model.addAttribute("annonces", mesAnnonces);
        model.addAttribute("currentUsername", username);
        model.addAttribute("pageTitle", "Mes annonces");
        model.addAttribute("totalAnnonces", mesAnnonces.size());
        model.addAttribute("favorisIds", favorisIds);  // Important pour les cœurs

        // ✅ AJOUTEZ CES ATTRIBUTS POUR LES STATISTIQUES :
        // model.addAttribute("totalViews", calculateTotalViews(mesAnnonces));  // Nouvelle méthode
        model.addAttribute("totalMessages", 0);  // À implémenter plus tard

        // ✅ Ajoutez aussi isAdmin si vous l'utilisez
        boolean isAdmin = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        model.addAttribute("isAdmin", isAdmin);

        return "mesannonces";
    }


    @PostMapping("/annonce/{id}/contact")
    public String contactSeller(@PathVariable Long id,
                                @RequestParam String message,
                                Principal principal,
                                RedirectAttributes redirectAttributes) {

        messageService.sendMessage(id, message, principal.getName());
        redirectAttributes.addFlashAttribute("success", "Message sent to seller!");
        return "redirect:/annonces/" + id;  // ❌ ERREUR : devrait être "annonces"
    }
    @GetMapping("/annonces/{id}")
    public String viewAnnonce(@PathVariable Long id,
                              Model model,
                              HttpSession session,
                              @AuthenticationPrincipal UserDetails userDetails,
                              HttpServletRequest request) {

        // 1. Récupérer l'annonce avec incrémentation des vues
        Annonce annonce = annonceService.findByIdAndIncrementViews(id);

        // 2. Récupérer les statistiques de notation depuis le service
        Double averageRating = annonceService.getAverageRating(id);
        Integer totalRatings = annonceService.getTotalRatings(id);

        // 3. Initialiser les champs de l'annonce si null (pour compatibilité)
        if (annonce.getAverageRating() == null) {
            annonce.setAverageRating(averageRating != null ? averageRating : 0.0);
        }
        if (annonce.getRatingCount() == null) {
            annonce.setRatingCount(totalRatings != null ? totalRatings : 0);
        }

        // 4. Ajouter les attributs au modèle
        model.addAttribute("annonce", annonce);
        model.addAttribute("averageRating", averageRating != null ? averageRating : 0.0);
        model.addAttribute("totalRatings", totalRatings != null ? totalRatings : 0);

        // 5. Récupérer toutes les notes pour cette annonce
        List<Rating> ratings = ratingRepository.findByAnnonceId(id);
        model.addAttribute("ratings", ratings);

        // 6. Gestion utilisateur connecté
        if (userDetails != null) {
            String username = userDetails.getUsername();
            model.addAttribute("currentUsername", username);

            boolean isOwner = annonceService.isOwner(id, username);
            model.addAttribute("isOwner", isOwner);

            boolean isAdmin = userDetails.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            model.addAttribute("isAdmin", isAdmin);

            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

            // Récupérer la note de l'utilisateur pour cette annonce
            Integer userRating = annonceService.getUserRating(id, user.getId());
            model.addAttribute("userRating", userRating);

            // Vérifier si l'utilisateur a déjà noté
            boolean hasRated = userRating != null;
            model.addAttribute("hasRated", hasRated);

            // Si l'utilisateur a déjà noté, récupérer l'objet Rating complet
            if (hasRated) {
                Optional<Rating> userRatingObj = ratingRepository.findByAnnonceIdAndUserId(id, user.getId());
                userRatingObj.ifPresent(rating -> model.addAttribute("userRatingObj", rating));
            }

        } else {
            // Utilisateur non connecté
            model.addAttribute("userRating", null);
            model.addAttribute("hasRated", false);
            model.addAttribute("currentUsername", null);
            model.addAttribute("isOwner", false);
            model.addAttribute("isAdmin", false);
        }

        // 7. Compter les annonces du vendeur
        Long vendeurAnnoncesCount = 0L;
        if (annonce.getVendeur() != null) {
            vendeurAnnoncesCount = annonceService.countByVendeurId(annonce.getVendeur().getId());
        }
        model.addAttribute("vendeurAnnoncesCount", vendeurAnnoncesCount);

        // 8. Historique des annonces (optionnel)
        if (session != null) {
            List<Long> historic = (List<Long>) session.getAttribute("historic");
            if (historic == null) historic = new ArrayList<>();

            historic.remove(id);
            historic.add(0, id);
            if (historic.size() > 5) historic = historic.subList(0, 5);

            session.setAttribute("historic", historic);
        }

        return "annoncedetail";
    }
   }