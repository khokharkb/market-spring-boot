package org.example.market.user;

import org.example.market.annonce.AnnonceService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@CrossOrigin(origins = {"http://localhost:3000", "capacitor://localhost", "http://localhost"})
@RequestMapping("/profil")
public class UserController {

    private final UserRepository userRepository;
    private final AnnonceService annonceService;

    public UserController(UserRepository userRepository, AnnonceService annonceService) {
        this.userRepository = userRepository;
        this.annonceService = annonceService;
    }

    // Page "Mon Profil"
    @GetMapping
    public String monProfil(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails == null) {
            return "redirect:/login";
        }

        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        // Récupérer les annonces de l'utilisateur
        int nombreAnnonces = annonceService.countByVendeurUsername(user.getUsername());

        model.addAttribute("user", user);
        model.addAttribute("nombreAnnonces", nombreAnnonces);

        return "profil";
    }

    // Formulaire de modification du profil
    @GetMapping("/edit")
    public String editProfilForm(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        model.addAttribute("user", user);
        return "edit-profil";
    }

    // Mettre à jour le profil
    @PostMapping("/update")
    public String updateProfil(@ModelAttribute User updatedUser,
                               @AuthenticationPrincipal UserDetails userDetails,
                               RedirectAttributes redirectAttributes) {

        User existingUser = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        // Mettre à jour les champs
        existingUser.setNom(updatedUser.getNom());
        existingUser.setPrenom(updatedUser.getPrenom());
        existingUser.setEmail(updatedUser.getEmail());
        existingUser.setTelephone(updatedUser.getTelephone());
        existingUser.setAdresse(updatedUser.getAdresse());
        existingUser.setVille(updatedUser.getVille());

        userRepository.save(existingUser);

        redirectAttributes.addFlashAttribute("success", "Profil mis à jour avec succès !");
        return "redirect:/profil";
    }
}