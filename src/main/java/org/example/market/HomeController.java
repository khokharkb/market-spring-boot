package org.example.market;

import org.example.market.annonce.Annonce;
import org.example.market.annonce.AnnonceService;
import org.example.market.favoris.Favori;
import org.example.market.favoris.FavoriService;
import org.example.market.message.Message;
import org.example.market.message.MessageService;
import org.example.market.panier.PanierService;
import org.example.market.user.User;
import org.example.market.user.UserRepository;
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
    private final PanierService panierService;
    private final UserRepository userRepository;
    private final FavoriService favoriService;

    public HomeController(AnnonceService annonceService,
                          MessageService messageService,
                          PanierService panierService,
                          UserRepository userRepository,
                          FavoriService favoriService) {
        this.annonceService = annonceService;
        this.messageService = messageService;
        this.panierService = panierService;
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

        // Message affiché après la déconnexion
        if (logout != null && logout.equals("true")) {
            model.addAttribute("logoutMessage", "Vous avez été déconnecté avec succès.");
        }

        List<Annonce> annonces = annonceService.findAll();

        // Filtrer par recherche
        if (search != null && !search.trim().isEmpty()) {
            final String searchLower = search.toLowerCase();
            annonces = annonces.stream()
                    .filter(a -> a.getTitre() != null && a.getTitre().toLowerCase().contains(searchLower) ||
                            a.getDescription() != null && a.getDescription().toLowerCase().contains(searchLower))
                    .collect(Collectors.toList());
        }

        // Filtrer par catégorie
        if (categorie != null && !categorie.trim().isEmpty()) {
            annonces = annonces.stream()
                    .filter(a -> categorie.equals(a.getCategorie()))
                    .collect(Collectors.toList());
        }

        // Catégories distinctes
        Set<String> categories = annonces.stream()
                .map(Annonce::getCategorie)
                .filter(cat -> cat != null && !cat.trim().isEmpty())
                .collect(Collectors.toSet());

        // La page d'accueil n'affiche que les 12 premières annonces
        List<Annonce> featuredAnnonces = annonces.stream()
                .limit(12)
                .collect(Collectors.toList());

        model.addAttribute("annonces", featuredAnnonces);
        model.addAttribute("allAnnonces", annonces);
        model.addAttribute("categories", categories);
        model.addAttribute("search", search);
        model.addAttribute("selectedCategorie", categorie);

        if (principal != null) {
            String username = principal.getName();

            long unreadCount = messageService.countUnreadMessages(username);
            model.addAttribute("unreadCount", unreadCount);

            // 3 derniers messages pour l'aperçu dans la barre de navigation
            List<Message> recentMessages = messageService.getRecentMessages(username, 3);
            model.addAttribute("recentMessages", recentMessages);

            model.addAttribute("currentUsername", username);

            try {
                User user = userRepository.findByUsername(username)
                        .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
                Integer cartItemCount = panierService.getCartItemCount(user.getId());
                model.addAttribute("cartItemCount", cartItemCount);
            } catch (Exception e) {
                model.addAttribute("cartItemCount", 0);
            }

        } else {
            model.addAttribute("unreadCount", 0);
            model.addAttribute("recentMessages", List.of());
            model.addAttribute("currentUsername", null);
            model.addAttribute("cartItemCount", 0);
        }

        return "home";
    }

    @GetMapping("/favori")
    public String favoriPage(Principal principal, Model model) {
        if (principal == null) {
            return "redirect:/login";
        }

        String username = principal.getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        List<Favori> favoris = favoriService.getFavorisByUser(user);

        List<Long> favoriteAnnonceIds = favoris.stream()
                .map(favori -> favori.getAnnonce().getId())
                .collect(Collectors.toList());

        model.addAttribute("favoris", favoris);
        model.addAttribute("favoriteAnnonceIds", favoriteAnnonceIds);
        model.addAttribute("currentUsername", username);

        // Données de la barre de navigation (messages, panier...)
        long unreadCount = messageService.countUnreadMessages(username);
        model.addAttribute("unreadCount", unreadCount);

        List<Message> recentMessages = messageService.getRecentMessages(username, 3);
        model.addAttribute("recentMessages", recentMessages);

        Integer cartItemCount = panierService.getCartItemCount(user.getId());
        model.addAttribute("cartItemCount", cartItemCount);

        return "favori";
    }
}