package org.example.market.panier;

import org.example.market.annonce.Annonce;
import org.example.market.annonce.AnnonceRepository;
import org.example.market.user.User;
import org.example.market.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PanierService {

    private final PanierRepository panierRepository;
    private final AnnonceRepository annonceRepository;
    private final UserRepository userRepository;

    // Ajouter un article au panier
    @Transactional
    public Panier ajouterAuPanier(Long userId, Long annonceId) {
        // Vérifier si l'utilisateur existe
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec l'ID: " + userId));

        // Vérifier si l'annonce existe
        Annonce annonce = annonceRepository.findById(annonceId)
                .orElseThrow(() -> new RuntimeException("Annonce non trouvée avec l'ID: " + annonceId));

        // Empêcher l'utilisateur d'ajouter sa propre annonce au panier
        if (annonce.getVendeur() != null && annonce.getVendeur().getId().equals(userId)) {
            throw new RuntimeException("Vous ne pouvez pas ajouter votre propre annonce au panier");
        }

        // Vérifier si l'article est déjà dans le panier
        Optional<Panier> existing = panierRepository.findByUserIdAndAnnonceId(userId, annonceId);

        if (existing.isPresent()) {
            // Augmenter la quantité
            Panier panier = existing.get();
            panier.setQuantite(panier.getQuantite() + 1);
            return panierRepository.save(panier);
        } else {
            // Nouvel article
            Panier panier = new Panier();
            panier.setUser(user);
            panier.setAnnonce(annonce);
            panier.setQuantite(1);
            return panierRepository.save(panier);
        }
    }

    // Obtenir le panier d'un utilisateur
    public List<Panier> getPanierUtilisateur(Long userId) {
        return panierRepository.findByUserId(userId);
    }

    // Retirer un article du panier (diminuer la quantité ou supprimer)
    @Transactional
    public void retirerDuPanier(Long userId, Long annonceId) {
        Optional<Panier> panierOpt = panierRepository.findByUserIdAndAnnonceId(userId, annonceId);
        if (panierOpt.isPresent()) {
            Panier panier = panierOpt.get();
            if (panier.getQuantite() > 1) {
                panier.setQuantite(panier.getQuantite() - 1);
                panierRepository.save(panier);
            } else {
                panierRepository.deleteByUserIdAndAnnonceId(userId, annonceId);
            }
        }
    }

    // Supprimer complètement un article du panier (peu importe la quantité)
    @Transactional
    public void supprimerDuPanier(Long userId, Long annonceId) {
        panierRepository.deleteByUserIdAndAnnonceId(userId, annonceId);
    }

    // Vider le panier d'un utilisateur
    @Transactional
    public void viderPanier(Long userId) {
        panierRepository.deleteByUserId(userId);
    }

    // Calculer le total du panier
    public Double calculerTotalPanier(Long userId) {
        List<Panier> panierItems = panierRepository.findByUserId(userId);
        return panierItems.stream()
                .mapToDouble(item -> {
                    if (item.getAnnonce() != null && item.getAnnonce().getPrix() != null) {
                        return item.getAnnonce().getPrix() * item.getQuantite();
                    }
                    return 0.0;
                })
                .sum();
    }

    // Pour afficher le nombre d'articles dans le panier (dans la navbar)
    public Integer getCartItemCount(Long userId) {
        List<Panier> panierItems = panierRepository.findByUserId(userId);
        return panierItems.stream()
                .mapToInt(Panier::getQuantite)
                .sum();
    }
}