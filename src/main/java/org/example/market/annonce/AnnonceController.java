package org.example.market.annonce;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@Controller
@CrossOrigin(origins = {"http://localhost:3000", "capacitor://localhost", "http://localhost"})
public class AnnonceController {

    private final AnnonceService annonceService;
    private final PanierService panierService;
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
            log.info("Dossier des images : {}", rootLocation.toAbsolutePath());
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize storage!", e);
        }
    }

    // Liste des annonces, avec recherche et filtre par catégorie
    @GetMapping("/annonces")
    public String getAnnonces(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String category,
            Model model, @AuthenticationPrincipal UserDetails userDetails) {
        List<Annonce> annonces = annonceService.findAll();
        model.addAttribute("annonces", annonces);

        // Filtrer par recherche
        if (search != null && !search.trim().isEmpty()) {
            final String searchLower = search.toLowerCase();
            annonces = annonces.stream()
                    .filter(a ->
                            (a.getTitre() != null && a.getTitre().toLowerCase().contains(searchLower)) ||
                                    (a.getDescription() != null && a.getDescription().toLowerCase().contains(searchLower))
                    )
                    .collect(Collectors.toList());
        }

        // Filtrer par catégorie
        if (category != null && !category.trim().isEmpty()) {
            annonces = annonces.stream()
                    .filter(a -> category.equals(a.getCategorie()))
                    .collect(Collectors.toList());
        }

        model.addAttribute("annonces", annonces);

        // Pour pré-remplir le formulaire de recherche
        model.addAttribute("search", search);
        model.addAttribute("category", category);

        // Le template a besoin de l'utilisateur courant pour afficher les boutons du propriétaire
        if (userDetails != null) {
            String username = userDetails.getUsername();
            model.addAttribute("currentUsername", username);

            boolean isAdmin = userDetails.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            model.addAttribute("isAdmin", isAdmin);

            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // IDs des favoris, pour afficher les cœurs remplis
            List<Long> favorisIds = favoriService.getFavorisByUser(user).stream()
                    .map(favori -> favori.getAnnonce().getId())
                    .collect(Collectors.toList());

            model.addAttribute("favorisIds", favorisIds);
        } else {
            model.addAttribute("favorisIds", new ArrayList<Long>());
        }

        // Statistiques affichées en haut de la page
        long totalAnnonces = annonceService.countAll();
        long vendeursCount = annonceService.countVendeursDistinct();
        List<String> categories = annonceService.findAllCategories();

        model.addAttribute("totalAnnonces", totalAnnonces);
        model.addAttribute("vendeursCount", vendeursCount);
        model.addAttribute("categoriesCount", categories);

        return "annonces";
    }

    // Formulaire de création d'une annonce
    @GetMapping("/annonces/new")
    public String showCreateForm(Model model) {
        model.addAttribute("annonce", new Annonce());
        return "annonce-form";
    }

    // Enregistrement d'une nouvelle annonce
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
                log.error("Échec de la sauvegarde de l'image", e);
                throw new RuntimeException("Échec de la sauvegarde de l'image: " + e.getMessage());
            }
        } else {
            annonce.setImagePath("default.jpg");
        }

        annonceService.save(annonce);
        return "redirect:/annonces";
    }

    // Formulaire de modification
    @GetMapping("/annonces/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model,
                               @AuthenticationPrincipal UserDetails userDetails,
                               RedirectAttributes redirectAttributes) {

        // Seul le propriétaire peut modifier (pas l'admin)
        if (!annonceService.isOwner(id, userDetails.getUsername())) {
            redirectAttributes.addFlashAttribute("error", "❌ Vous n'êtes pas autorisé à modifier cette annonce");
            return "redirect:/annonces";
        }

        Annonce annonce = annonceService.findById(id);
        model.addAttribute("annonce", annonce);
        model.addAttribute("isEdit", true);

        return "annonce-form";
    }

    // Mise à jour d'une annonce
    @PostMapping("/annonces/{id}/edit")
    public String updateAnnonce(@PathVariable Long id,
                                @ModelAttribute Annonce annonceDetails,
                                @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                                @AuthenticationPrincipal UserDetails userDetails,
                                RedirectAttributes redirectAttributes) {

        if (!annonceService.isOwner(id, userDetails.getUsername())) {
            redirectAttributes.addFlashAttribute("error", "❌ Vous n'êtes pas autorisé à modifier cette annonce");
            return "redirect:/annonces";
        }

        Annonce existingAnnonce = annonceService.findById(id);

        // Nouvelle image si une a été envoyée, sinon on garde l'ancienne
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
                log.error("Échec de l'upload de l'image", e);
                redirectAttributes.addFlashAttribute("error", "Erreur lors du upload de l'image");
                return "redirect:/annonces/" + id + "/edit";
            }
        } else {
            annonceDetails.setImagePath(existingAnnonce.getImagePath());
        }

        annonceService.update(id, annonceDetails);
        redirectAttributes.addFlashAttribute("success", "✅ Annonce modifiée avec succès");

        return "redirect:/annonces";
    }

    // Suppression d'une annonce
    @PostMapping("/annonces/{id}/delete")
    public String deleteAnnonce(@PathVariable Long id,
                                @AuthenticationPrincipal UserDetails userDetails,
                                RedirectAttributes redirectAttributes) {

        // Le propriétaire ou un admin peut supprimer
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

    // Images des annonces
    @GetMapping("/uploads/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {
        try {
            Path root = rootLocation.toAbsolutePath().normalize();
            Path file = root.resolve(filename).normalize();
            // Refuse les chemins qui sortent du dossier uploads (ex : "../")
            if (!file.startsWith(root)) {
                return ResponseEntity.notFound().build();
            }
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

    // Annonces de l'utilisateur connecté
    @GetMapping("/mesannonces")
    public String mesAnnonces(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return "redirect:/login";
        }

        String username = userDetails.getUsername();
        List<Annonce> mesAnnonces = annonceService.findByVendeurUsername(username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Long> favorisIds = favoriService.getFavorisByUser(user).stream()
                .map(favori -> favori.getAnnonce().getId())
                .collect(Collectors.toList());

        model.addAttribute("annonces", mesAnnonces);
        model.addAttribute("currentUsername", username);
        model.addAttribute("pageTitle", "Mes annonces");
        model.addAttribute("totalAnnonces", mesAnnonces.size());
        model.addAttribute("favorisIds", favorisIds);
        // TODO : compter les vrais messages reçus
        model.addAttribute("totalMessages", 0);

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
        return "redirect:/annonces/" + id;
    }

    @GetMapping("/annonces/{id}")
    public String viewAnnonce(@PathVariable Long id,
                              Model model,
                              HttpSession session,
                              @AuthenticationPrincipal UserDetails userDetails,
                              HttpServletRequest request) {

        // Chaque visite incrémente le compteur de vues
        Annonce annonce = annonceService.findByIdAndIncrementViews(id);

        Double averageRating = annonceService.getAverageRating(id);
        Integer totalRatings = annonceService.getTotalRatings(id);

        // Les anciennes annonces peuvent avoir ces champs à null
        if (annonce.getAverageRating() == null) {
            annonce.setAverageRating(averageRating != null ? averageRating : 0.0);
        }
        if (annonce.getRatingCount() == null) {
            annonce.setRatingCount(totalRatings != null ? totalRatings : 0);
        }

        model.addAttribute("annonce", annonce);
        model.addAttribute("averageRating", averageRating != null ? averageRating : 0.0);
        model.addAttribute("totalRatings", totalRatings != null ? totalRatings : 0);

        List<Rating> ratings = ratingRepository.findByAnnonceId(id);
        model.addAttribute("ratings", ratings);

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

            Integer userRating = annonceService.getUserRating(id, user.getId());
            model.addAttribute("userRating", userRating);

            boolean hasRated = userRating != null;
            model.addAttribute("hasRated", hasRated);

            if (hasRated) {
                Optional<Rating> userRatingObj = ratingRepository.findByAnnonceIdAndUserId(id, user.getId());
                userRatingObj.ifPresent(rating -> model.addAttribute("userRatingObj", rating));
            }

        } else {
            model.addAttribute("userRating", null);
            model.addAttribute("hasRated", false);
            model.addAttribute("currentUsername", null);
            model.addAttribute("isOwner", false);
            model.addAttribute("isAdmin", false);
        }

        Long vendeurAnnoncesCount = 0L;
        if (annonce.getVendeur() != null) {
            vendeurAnnoncesCount = annonceService.countByVendeurId(annonce.getVendeur().getId());
        }
        model.addAttribute("vendeurAnnoncesCount", vendeurAnnoncesCount);

        // Historique des 5 dernières annonces consultées, gardé en session
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