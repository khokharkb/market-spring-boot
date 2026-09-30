package org.example.market.rating;

import org.example.market.annonce.AnnonceService;
import org.example.market.user.User;
import org.example.market.user.UserRepository;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/annonces/{id}")
public class RatingController {

    private final AnnonceService annonceService;
    private final UserRepository userRepository;
    private final RatingRepository ratingRepository; // Ajoutez cette dépendance

    public RatingController(AnnonceService annonceService,
                            UserRepository userRepository,
                            RatingRepository ratingRepository) {
        this.annonceService = annonceService;
        this.userRepository = userRepository;
        this.ratingRepository = ratingRepository;
    }

    /**
     * Endpoint pour noter une annonce
     */
    @PostMapping("/rate")
    public String rateAnnonce(@PathVariable Long id,
                              @RequestParam("stars") Integer stars,
                              @RequestParam(value = "comment", required = false) String comment,
                              @AuthenticationPrincipal UserDetails userDetails,
                              RedirectAttributes redirectAttributes) {

        log.info("=== DÉBUT : Notation de l'annonce {} ===", id);
        log.info("Note : {}", stars);
        log.info("Commentaire : {}", comment);
        log.info("Utilisateur connecté : {}", userDetails != null ? userDetails.getUsername() : "null");

        if (userDetails == null) {
            log.error("Utilisateur non connecté");
            redirectAttributes.addFlashAttribute("error", "Vous devez être connecté pour noter");
            return "redirect:/login";
        }

        try {
            // Récupérer l'utilisateur
            String username = userDetails.getUsername();
            log.info("Recherche de l'utilisateur : {}", username);

            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> {
                        log.error("Utilisateur {} non trouvé", username);
                        return new RuntimeException("Utilisateur non trouvé");
                    });

            log.info("Utilisateur trouvé : ID={}, Username={}", user.getId(), user.getUsername());

            // Vérifier que la note est valide
            if (stars < 1 || stars > 5) {
                log.error("Note invalide : {}", stars);
                redirectAttributes.addFlashAttribute("error", "La note doit être entre 1 et 5 étoiles");
                return "redirect:/annonces/" + id;
            }

            // Vérifier si l'utilisateur a déjà noté cette annonce
            boolean alreadyRated = ratingRepository.existsByAnnonceIdAndUserId(id, user.getId());
            log.info("L'utilisateur a déjà noté ? {}", alreadyRated);

            if (alreadyRated) {
                redirectAttributes.addFlashAttribute("error", "Vous avez déjà noté cette annonce");
                return "redirect:/annonces/" + id;
            }

            // Ajouter la note
            log.info("Appel de annonceService.rateAnnonce avec id={}, userId={}, stars={}, comment={}",
                    id, user.getId(), stars, comment);

            boolean success = annonceService.rateAnnonce(id, user.getId(), stars, comment);

            if (success) {
                log.info("Note enregistrée avec succès");

                // Récupérer les nouvelles valeurs pour les passer au modèle
                Double averageRating = annonceService.getAverageRating(id);
                Integer totalRatings = annonceService.getTotalRatings(id);

                log.info("Nouvelle moyenne : {}, Nombre total de notes : {}", averageRating, totalRatings);

                redirectAttributes.addFlashAttribute("success",
                        "Merci pour votre note de " + stars + " étoile" + (stars > 1 ? "s" : "") + " !");
                redirectAttributes.addFlashAttribute("averageRating", averageRating);
                redirectAttributes.addFlashAttribute("totalRatings", totalRatings);

            } else {
                log.error("Échec de l'enregistrement de la note");
                redirectAttributes.addFlashAttribute("error", "Erreur lors de l'enregistrement de la note");
            }

        } catch (RuntimeException e) {
            log.error("Erreur RuntimeException : {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            log.error("Erreur inattendue : {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Une erreur est survenue: " + e.getMessage());
        }

        log.info("=== FIN : Notation de l'annonce {} ===", id);
        return "redirect:/annonces/" + id;
    }

    /**
     * Endpoint pour supprimer sa note
     */
    @PostMapping("/rate/delete")
    public String deleteRating(@PathVariable Long id,
                               @AuthenticationPrincipal UserDetails userDetails,
                               RedirectAttributes redirectAttributes) {

        log.info("=== DÉBUT : Suppression de note pour l'annonce {} ===", id);

        if (userDetails == null) {
            redirectAttributes.addFlashAttribute("error", "Vous devez être connecté");
            return "redirect:/login";
        }

        try {
            // Récupérer l'utilisateur
            String username = userDetails.getUsername();
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

            // Supprimer la note
            boolean success = annonceService.removeRating(id, user.getId());

            if (success) {
                redirectAttributes.addFlashAttribute("success", "Votre note a été supprimée");
                log.info("Note supprimée avec succès");
            } else {
                redirectAttributes.addFlashAttribute("error", "Impossible de supprimer la note");
                log.error("Échec de la suppression de la note");
            }

        } catch (Exception e) {
            log.error("Erreur lors de la suppression : {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Erreur: " + e.getMessage());
        }

        log.info("=== FIN : Suppression de note pour l'annonce {} ===", id);
        return "redirect:/annonces/" + id;
    }
}