package org.example.market.favoris;

import lombok.RequiredArgsConstructor;
import org.example.market.annonce.Annonce;
import org.example.market.annonce.AnnonceRepository;
import org.example.market.user.User;
import org.example.market.user.UserRepository;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;

@Controller
@RequestMapping("/favoris")
@RequiredArgsConstructor
public class FavoriController {

    private final FavoriRepository favoriRepository;
    private final AnnonceRepository annonceRepository;
    private final UserRepository userRepository;
    private final FavoriService favoriService;

    // === AJOUTER UNE ANNONCE AUX FAVORIS ===
    @PostMapping("/ajouter/{annonceId}")
    public String ajouterFavori(@PathVariable Long annonceId,
                                @AuthenticationPrincipal UserDetails userDetails,
                                RedirectAttributes redirectAttributes) {

        // Vérifier si l'utilisateur est connecté
        if (userDetails == null) {
            redirectAttributes.addFlashAttribute("error", "Vous devez être connecté pour ajouter aux favoris");
            return "redirect:/login";
        }

        try {
            // Récupérer l'utilisateur depuis la base de données
            String username = userDetails.getUsername();
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

            // Récupérer l'annonce
            Annonce annonce = annonceRepository.findById(annonceId)
                    .orElseThrow(() -> new RuntimeException("Annonce non trouvée"));

            // Vérifier si l'annonce n'est pas déjà dans les favoris
            boolean dejaFavori = favoriRepository.existsByUserAndAnnonce(user, annonce);
            if (dejaFavori) {
                redirectAttributes.addFlashAttribute("error", "Cette annonce est déjà dans vos favoris");
                return "redirect:/annonces";
            }

            // Créer et sauvegarder le favori
            Favori favori = Favori.builder()
                    .user(user)  // CECI EST IMPORTANT - ne pas oublier l'utilisateur!
                    .annonce(annonce)
                    .dateAjout(LocalDateTime.now())
                    .build();

            favoriRepository.save(favori);

            redirectAttributes.addFlashAttribute("success", "Annonce ajoutée aux favoris !");

        } catch (Exception e) {
            e.printStackTrace(); // Pour le débogage
            redirectAttributes.addFlashAttribute("error",
                    "Erreur lors de l'ajout aux favoris: " + e.getMessage());
        }

        return "redirect:/annonces";
    }

    // === SUPPRIMER UNE ANNONCE DES FAVORIS ===
    @PostMapping("/supprimer/{annonceId}")
    public String supprimerFavori(@PathVariable Long annonceId,
                                  @AuthenticationPrincipal UserDetails userDetails,
                                  RedirectAttributes redirectAttributes) {

        if (userDetails == null) {
            redirectAttributes.addFlashAttribute("error", "Vous devez être connecté");
            return "redirect:/login";
        }

        try {
            String username = userDetails.getUsername();
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

            // Appel au service pour supprimer le favori
            favoriService.supprimerFavori(annonceId, user);

            redirectAttributes.addFlashAttribute("success", "Annonce retirée des favoris");

        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error",
                    "Erreur lors de la suppression: " + e.getMessage());
        }

        return "redirect:/annonces";
    }

    // === PAGE DES FAVORIS ===
    @GetMapping
    public String mesFavoris(@AuthenticationPrincipal UserDetails userDetails,
                             RedirectAttributes redirectAttributes,
                             org.springframework.ui.Model model) {

        if (userDetails == null) {
            redirectAttributes.addFlashAttribute("error", "Vous devez être connecté pour voir vos favoris");
            return "redirect:/login";
        }

        try {
            // Récupérer l'utilisateur
            String username = userDetails.getUsername();
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

            // Récupérer les favoris de l'utilisateur
            var favoris = favoriRepository.findByUserOrderByDateAjoutDesc(user);

            model.addAttribute("favoris", favoris);
            model.addAttribute("pageTitle", "Mes Favoris");

            return "favoris";

        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error",
                    "Erreur lors du chargement des favoris: " + e.getMessage());
            return "redirect:/annonces";
        }
    }
}