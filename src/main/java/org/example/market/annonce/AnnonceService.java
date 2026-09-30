package org.example.market.annonce;

import jakarta.transaction.Transactional;
import org.example.market.rating.Rating;
import org.example.market.rating.RatingRepository;
import org.example.market.user.User;
import org.example.market.user.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AnnonceService {

    private final AnnonceRepository repo;
    private final UserRepository userRepository;
    private final RatingRepository ratingRepository;


    public AnnonceService(AnnonceRepository repo, UserRepository userRepository,RatingRepository ratingRepository) {
        this.repo = repo;
        this.userRepository = userRepository;
        this.ratingRepository = ratingRepository;
    }
    public int countByVendeurUsername(String username) {
        return repo.countByVendeurUsername(username);
    }
    public List<Annonce> findAll() {
        List<Annonce> list = repo.findAll();
        System.out.println("---- DEBUG annonces: " + list.size());
        return list;
    }

    public Annonce findById(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Annonce non trouvée"));
    }

    public Annonce save(Annonce a) {
        return repo.save(a);
    }
    // MÉTHODE POUR METTRE À JOUR UNE ANNONCE
    public Annonce update(Long id, Annonce annonceDetails) {
        Annonce existingAnnonce = findById(id);

        existingAnnonce.setTitre(annonceDetails.getTitre());
        existingAnnonce.setDescription(annonceDetails.getDescription());
        existingAnnonce.setPrix(annonceDetails.getPrix());
        existingAnnonce.setCategorie(annonceDetails.getCategorie());

        // Garder l'image existante si aucune nouvelle image n'est fournie
        if (annonceDetails.getImagePath() != null && !annonceDetails.getImagePath().isEmpty()) {
            existingAnnonce.setImagePath(annonceDetails.getImagePath());
        }

        return repo.save(existingAnnonce);
    }
    public boolean canDelete(Annonce annonce) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();

        boolean isAdmin = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role -> role.equals("ROLE_ADMIN"));

        boolean isOwner = annonce.getVendeur() != null &&
                annonce.getVendeur().getUsername().equals(currentUsername);

        return isAdmin || isOwner;
    }
    // MÉTHODE POUR VÉRIFIER SI L'UTILISATEUR PEUT SUPPRIMER (version avec ID)
    public boolean canDelete(Long annonceId) {
        Annonce annonce = findById(annonceId);
        return canDelete(annonce);
    }
    // MÉTHODE POUR SUPPRIMER UNE ANNONCE
    @Transactional
    public void delete(Long id) {
        Annonce annonce = findById(id);

        // Vérifier si l'utilisateur a le droit de supprimer
        if (!canDelete(annonce)) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Vous n'avez pas le droit de supprimer cette annonce"
            );
        }

        repo.deleteById(id);
    }

    // MÉTHODE POUR VÉRIFIER SI L'UTILISateur EST LE PROPRIÉTAIRE
    public boolean isOwner(Long annonceId, String username) {
        Optional<Annonce> annonceOpt = repo.findById(annonceId);
        return annonceOpt.isPresent() && annonceOpt.get().isOwner(username);
    }
    // MÉTHODE POUR TROUVER LES ANNONCES D'UN UTILISATEUR
    public List<Annonce> findByVendeurUsername(String username) {
        return repo.findByVendeurUsername(username);
    }
    // Dans AnnonceService.java
    public long count() {
        return repo.count();
    }

    public void deleteByUserId(Long userId) {
        // Implémentez cette méthode pour supprimer toutes les annonces d'un utilisateur
        // Par exemple :
        List<Annonce> annonces = repo.findByVendeurId(userId);
        repo.deleteAll(annonces);
    }
    // Méthode pour compter toutes les annonces
    public long countAll() {
        return repo.count();
    }

    // Méthode pour compter les vendeurs distincts
    public long countVendeursDistinct() {
        return repo.countDistinctVendeurs();
    }

    // Méthode pour récupérer toutes les catégories
    public List<String> findAllCategories() {
        return repo.findAllDistinctCategories();
    }

    // Méthode pour rechercher des annonces avec filtres (optionnel)
    public List<Annonce> searchAnnonces(String search, String category) {
        if (search != null && !search.isEmpty() && category != null && !category.isEmpty()) {
            return repo.findByTitreContainingIgnoreCaseAndCategorie(search, category);
        } else if (search != null && !search.isEmpty()) {
            return repo.findByTitreContainingIgnoreCase(search);
        } else if (category != null && !category.isEmpty()) {
            return repo.findByCategorie(category);
        } else {
            return repo.findAll();
        }
    }
    // Méthode pour incrémenter les vues
    @Transactional
    public void incrementViews(Long annonceId) {
        Annonce annonce = repo.findById(annonceId)
                .orElseThrow(() -> new RuntimeException("Annonce non trouvée"));

        annonce.incrementViews();
       repo.save(annonce);
    }

    // Méthode pour récupérer une annonce avec incrémentation des vues
    @Transactional
    public Annonce findByIdAndIncrementViews(Long id) {
        Annonce annonce = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Annonce non trouvée"));

        // Incrémenter les vues
        annonce.incrementViews();
        return repo.save(annonce);
    }
    // Méthode pour compter les annonces d'un vendeur
    public Long countByVendeurId(Long vendeurId) {
        return repo.countByVendeurId(vendeurId);
        // OU si vous utilisez la query personnalisée :
        // return annonceRepository.countAnnoncesByVendeurId(vendeurId);
    }

    // Autres méthodes utiles pour les statistiques
    public Long countTotalAnnonces() {
        return repo.count();
    }

    public Long countAnnoncesByCategorie(String categorie) {
        return repo.countByCategorie(categorie);
    }

    public Annonce rateAnnonce(Long annonceId, Long userId, Integer stars) {
        Annonce annonce = findById(annonceId);

        // Empêcher le propriétaire de noter sa propre annonce
        if (annonce.getVendeur() != null && annonce.getVendeur().getId().equals(userId)) {
            throw new RuntimeException("Vous ne pouvez pas noter votre propre annonce");
        }

        // Ajouter la note
        annonce.addRating(userId, stars);

        // Sauvegarder
        return repo.save(annonce);
    }

    /**
     * Récupère la note d'un utilisateur pour une annonce
     * @param annonceId ID de l'annonce
     * @param userId ID de l'utilisateur
     * @return La note (1-5) ou null si pas noté
     */
    public Integer getUserRating(Long annonceId, Long userId) {
        Annonce annonce = findById(annonceId);
        return annonce.getUserRating(userId);
    }

    /**
     * Vérifie si un utilisateur peut noter cette annonce
     * @param annonceId ID de l'annonce
     * @param userId ID de l'utilisateur
     * @return true si l'utilisateur peut noter
     */
    public boolean canUserRate(Long annonceId, Long userId) {
        Annonce annonce = findById(annonceId);

        // Vérifier que l'utilisateur n'est pas le propriétaire
        if (annonce.getVendeur() != null && annonce.getVendeur().getId().equals(userId)) {
            return false;
        }

        // Pour l'instant, on autorise tous les utilisateurs connectés
        // (on pourrait ajouter d'autres vérifications plus tard)
        return true;
    }

    /**
     * Supprime la note d'un utilisateur pour une annonce
     * @param annonceId ID de l'annonce
     * @param userId ID de l'utilisateur

    public void removeRating(Long annonceId, Long userId) {
        Annonce annonce = findById(annonceId);
        annonce.removeRating(userId);
        repo.save(annonce);
    } */
    public boolean rateAnnonce(Long annonceId, Long userId, Integer stars, String comment) {
        try {
            // Récupérer l'annonce
            Annonce annonce = repo.findById(annonceId)
                    .orElseThrow(() -> new RuntimeException("Annonce non trouvée"));

            // Récupérer l'utilisateur
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

            // Vérifier si la note existe déjà
            Optional<Rating> existingRating = ratingRepository.findByAnnonceIdAndUserId(annonceId, userId);

            if (existingRating.isPresent()) {
                // Mettre à jour la note existante
                Rating rating = existingRating.get();
                rating.setStars(stars);
                rating.setComment(comment);
                ratingRepository.save(rating);
                return true;
            } else {
                // Créer une nouvelle note
                Rating rating = new Rating(annonce, user, stars, comment);
                ratingRepository.save(rating);
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean removeRating(Long annonceId, Long userId) {
        try {
            ratingRepository.deleteByAnnonceIdAndUserId(annonceId, userId);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public Double getAverageRating(Long annonceId) {
        Double avg = ratingRepository.findAverageRatingByAnnonceId(annonceId);
        return avg != null ? Math.round(avg * 10.0) / 10.0 : 0.0; // Arrondir à 1 décimale
    }

    public Integer getTotalRatings(Long annonceId) {
        Long count = ratingRepository.countByAnnonceId(annonceId);
        return count != null ? count.intValue() : 0;
    }

    public boolean hasUserRated(Long annonceId, Long userId) {
        return ratingRepository.existsByAnnonceIdAndUserId(annonceId, userId);
    }

    public void updateRatingStats(Long id) {
    }
}


