package org.example.market;

import org.example.market.annonce.Annonce;
import org.example.market.annonce.AnnonceService;
import org.example.market.favoris.Favori;
import org.example.market.favoris.FavoriService;
import org.example.market.message.Message;
import org.example.market.message.MessageService;
import org.example.market.panier.PanierService; // Ajouter cette importation
import org.example.market.user.User;
import org.example.market.favoris.FavoriService;
import org.example.market.user.UserRepository;// Ajouter cette importation
import org.example.market.user.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@CrossOrigin(origins = {"http://localhost:3000", "capacitor://localhost", "http://localhost"})
public class HomeController {

    private final AnnonceService annonceService;
    private final MessageService messageService;
    private final PanierService panierService; // Ajouter ceci
    private final UserRepository userRepository; // Ajouter ceci
    private final FavoriService favoriService;

    public HomeController(AnnonceService annonceService,
                          MessageService messageService,
                          PanierService panierService, // Ajouter ceci
                          UserRepository userRepository,
                          FavoriService favoriService) { // Ajouter ceci
        this.annonceService = annonceService;
        this.messageService = messageService;
        this.panierService = panierService; // Ajouter ceci
        this.userRepository = userRepository;
        this.favoriService = favoriService;
    }

    @GetMapping("/")
    public String home(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String categorie,
            @RequestParam(required = false) String logout,
            Principal principal,
            Model model) {

        // If logout parameter is present, add a message
        if (logout != null && logout.equals("true")) {
            model.addAttribute("logoutMessage", "Vous avez été déconnecté avec succès.");
        }

        List<Annonce> annonces = annonceService.findAll();

        // Filter by search if provided
        if (search != null && !search.trim().isEmpty()) {
            final String searchLower = search.toLowerCase();
            annonces = annonces.stream()
                    .filter(a -> a.getTitre() != null && a.getTitre().toLowerCase().contains(searchLower) ||
                            a.getDescription() != null && a.getDescription().toLowerCase().contains(searchLower))
                    .collect(Collectors.toList());
        }

        // Filter by category if provided
        if (categorie != null && !categorie.trim().isEmpty()) {
            annonces = annonces.stream()
                    .filter(a -> categorie.equals(a.getCategorie()))
                    .collect(Collectors.toList());
        }

        // Get all unique categories
        Set<String> categories = annonces.stream()
                .map(Annonce::getCategorie)
                .filter(cat -> cat != null && !cat.trim().isEmpty())
                .collect(Collectors.toSet());

        // Limit to 12 announcements
        List<Annonce> featuredAnnonces = annonces.stream()
                .limit(12)
                .collect(Collectors.toList());

        // Add data to model
        model.addAttribute("annonces", featuredAnnonces);
        model.addAttribute("allAnnonces", annonces);
        model.addAttribute("categories", categories);
        model.addAttribute("search", search);
        model.addAttribute("selectedCategorie", categorie);

        if (principal != null) {
            String username = principal.getName();

            // Compter les messages non lus
            long unreadCount = messageService.countUnreadMessages(username);
            model.addAttribute("unreadCount", unreadCount);

            // Récupérer les 3 derniers messages récents
            List<Message> recentMessages = messageService.getRecentMessages(username, 3);
            model.addAttribute("recentMessages", recentMessages);

            // Ajouter le nom d'utilisateur pour le profil
            model.addAttribute("currentUsername", username);

            // Récupérer l'ID de l'utilisateur pour le panier
            try {
                User user = userRepository.findByUsername(username)
                        .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
                Integer cartItemCount = panierService.getCartItemCount(user.getId());
                model.addAttribute("cartItemCount", cartItemCount);
            } catch (Exception e) {
                model.addAttribute("cartItemCount", 0);
            }

        } else {
            // Si pas connecté, mettre 0 et liste vide
            model.addAttribute("unreadCount", 0);
            model.addAttribute("recentMessages", List.of());
            model.addAttribute("currentUsername", null);
            model.addAttribute("cartItemCount", 0);
        }

        return "home";
    }

    @GetMapping("/favori")
    public String favoriPage(Principal principal, Model model) {
        // Vérifier si l'utilisateur est connecté
        if (principal == null) {
            return "redirect:/login";
        }

        String username = principal.getName();

        // Récupérer l'utilisateur
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        // Récupérer les annonces favorites de l'utilisateur
        List<Favori> favoris = favoriService.getFavorisByUser(user);

        List<Long> favoriteAnnonceIds = favoris.stream()
                .map(favori -> favori.getAnnonce().getId())
                .collect(Collectors.toList());

        // Ajouter les données au modèle
        model.addAttribute("favoris", favoris);
        model.addAttribute("favoriteAnnonceIds", favoriteAnnonceIds);
        model.addAttribute("currentUsername", username);

        // Ajouter les autres attributs pour la navigation (messages, panier, etc.)
        long unreadCount = messageService.countUnreadMessages(username);
        model.addAttribute("unreadCount", unreadCount);

        List<Message> recentMessages = messageService.getRecentMessages(username, 3);
        model.addAttribute("recentMessages", recentMessages);

        Integer cartItemCount = panierService.getCartItemCount(user.getId());
        model.addAttribute("cartItemCount", cartItemCount);

        return "favori";
    }
}