package org.example.market.admin;

import org.example.market.user.User;
import org.example.market.user.UserRepository;
import org.example.market.annonce.AnnonceService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@CrossOrigin(origins = {"http://localhost:3000", "capacitor://localhost", "http://localhost"})
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')") // Seulement accessible aux admins
public class AdminController {

    private final UserRepository userRepository;
    private final AnnonceService annonceService;

    public AdminController(UserRepository userRepository, AnnonceService annonceService) {
        this.userRepository = userRepository;
        this.annonceService = annonceService;
    }

    // Tableau de bord admin
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        long userCount = userRepository.count();
        long annonceCount = annonceService.count();
        List<User> users = userRepository.findAll(); // Récupérer tous les utilisateurs

        model.addAttribute("userCount", userCount);
        model.addAttribute("annonceCount", annonceCount);
        model.addAttribute("users", users); // Pour afficher dans le tableau

        return "dashboard"; // CHANGÉ ICI : "dashboard" au lieu de "admin/dashboard"
    }

    @PostMapping("/delete-user/{id}")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            // D'abord supprimer les annonces de l'utilisateur
            annonceService.deleteByUserId(id);

            // Puis supprimer l'utilisateur
            userRepository.deleteById(id);

            redirectAttributes.addFlashAttribute("successMessage", "Utilisateur supprimé avec succès.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erreur lors de la suppression : " + e.getMessage());
        }

        return "redirect:/admin/dashboard";
    }

    // Activer/désactiver un utilisateur
    @PostMapping("/toggle-user/{id}")
    public String toggleUser(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        user.setActive(!user.isActive());
        userRepository.save(user);

        return "redirect:/admin/dashboard?success=Utilisateur modifié";
    }

    // Changer le rôle d'un utilisateur
    @PostMapping("/change-role/{id}")
    public String changeRole(@PathVariable Long id, @RequestParam String role) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        user.setRole(role);
        userRepository.save(user);

        return "redirect:/admin/dashboard?success=Rôle changé";
    }
}