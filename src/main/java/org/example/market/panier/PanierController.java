package org.example.market.panier;

import lombok.RequiredArgsConstructor;
import org.example.market.user.User;
import org.example.market.user.UserRepository; // Ajoutez cette importation
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/panier")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "capacitor://localhost", "http://localhost"})
public class PanierController {

    private final PanierService panierService;
    private final UserRepository userRepository; // Ajoutez ceci


    // Afficher le panier de l'utilisateur connecté
    @GetMapping
    public String afficherPanier(Model model, Authentication authentication) {
        // Récupérer l'ID de l'utilisateur connecté
        Long userId = getCurrentUserId(authentication);

        List<Panier> panierItems = panierService.getPanierUtilisateur(userId);
        Double total = panierService.calculerTotalPanier(userId);

        model.addAttribute("panierItems", panierItems);
        model.addAttribute("total", total);

        return "panier";
    }

    // Ajouter un article au panier
    @PostMapping("/add/{annonceId}")
    public String ajouterAuPanier(@PathVariable Long annonceId,
                                  Authentication authentication,
                                  RedirectAttributes redirectAttributes) {
        Long userId = getCurrentUserId(authentication);

        try {
            panierService.ajouterAuPanier(userId, annonceId);
            redirectAttributes.addFlashAttribute("success", "Article ajouté au panier !");
        } catch (Exception e) {
            e.printStackTrace(); // Pour le débogage
            redirectAttributes.addFlashAttribute("error", "Erreur lors de l'ajout au panier: " + e.getMessage());
        }

        return "redirect:/";
    }

    // Retirer un article du panier (diminuer quantité)
    @PostMapping("/retirer/{annonceId}")
    public String retirerDuPanier(@PathVariable Long annonceId,
                                  Authentication authentication,
                                  RedirectAttributes redirectAttributes) {
        Long userId = getCurrentUserId(authentication);

        try {
            panierService.retirerDuPanier(userId, annonceId);
            redirectAttributes.addFlashAttribute("success", "Quantité mise à jour.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la mise à jour.");
        }

        return "redirect:/panier";
    }

    // Supprimer complètement un article du panier
    @PostMapping("/supprimer/{annonceId}")
    public String supprimerDuPanier(@PathVariable Long annonceId,
                                    Authentication authentication,
                                    RedirectAttributes redirectAttributes) {
        Long userId = getCurrentUserId(authentication);

        try {
            panierService.supprimerDuPanier(userId, annonceId);
            redirectAttributes.addFlashAttribute("success", "Article retiré du panier.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la suppression.");
        }

        return "redirect:/panier";
    }

    // Vider tout le panier
    @PostMapping("/vider")
    public String viderPanier(Authentication authentication,
                              RedirectAttributes redirectAttributes) {
        Long userId = getCurrentUserId(authentication);

        try {
            panierService.viderPanier(userId);
            redirectAttributes.addFlashAttribute("success", "Panier vidé.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors du vidage du panier.");
        }

        return "redirect:/panier";
    }

    private Long getCurrentUserId(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("Utilisateur non connecté");
        }
        String username = authentication.getName();

        // Cherchez l'utilisateur par son nom d'utilisateur
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec le nom: " + username));

        return user.getId();
    }
}