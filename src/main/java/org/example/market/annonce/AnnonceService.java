package org.example.market.annonce;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
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
        return repo.findAll();
    }

    public Annonce findById(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Annonce non trouvée"));
    }

    public Annonce save(Annonce a) {
        return repo.save(a);
    }

    public Annonce update(Long id, Annonce annonceDetails) {
        Annonce existingAnnonce = findById(id);

        existingAnnonce.setTitre(annonceDetails.getTitre());
        existingAnnonce.setDescription(annonceDetails.getDescription());
        existingAnnonce.setPrix(annonceDetails.getPrix());
        existingAnnonce.setCategorie(annonceDetails.getCategorie());

        // On garde l'image existante si aucune nouvelle image n'est fournie
        if (annonceDetails.getImagePath() != null && !annonceDetails.getImagePath().isEmpty()) {
            existingAnnonce.setImagePath(annonceDetails.getImagePath());
        }

        return repo.save(existingAnnonce);
    }

    /** Le propriétaire de l'annonce ou un admin peut la supprimer. */
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

    public boolean canDelete(Long annonceId) {
        Annonce annonce = findById(annonceId);
        return canDelete(annonce);
    }

    @Transactional
    public void delete(Long id) {
        Annonce annonce = findById(id);

        if (!canDelete(annonce)) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Vous n'avez pas le droit de supprimer cette annonce"
            );
        }

        repo.deleteById(id);
    }

    public boolean isOwner(Long annonceId, String username) {
        Optional<Annonce> annonceOpt = repo.findById(annonceId);
        return annonceOpt.isPresent() && annonceOpt.get().isOwner(username);
    }

    public List<Annonce> findByVendeurUsername(String username) {
        return repo.findByVendeurUsername(username);
    }

    public long count() {
        return repo.count();
    }

    /** Supprime toutes les annonces d'un utilisateur (avant de supprimer son compte). */
    public void deleteByUserId(Long userId) {
        List<Annonce> annonces = repo.findByVendeurId(userId);
        repo.deleteAll(annonces);
    }

    public long countAll() {
        return repo.count();
    }

    public long countVendeursDistinct() {
        return repo.countDistinctVendeurs();
    }

    public List<String> findAllCategories() {
        return repo.findAllDistinctCategories();
    }

    /** Recherche par titre et/ou catégorie ; sans filtre, renvoie toutes les annonces. */
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

    @Transactional
    public void incrementViews(Long annonceId) {
        Annonce annonce = repo.findById(annonceId)
                .orElseThrow(() -> new RuntimeException("Annonce non trouvée"));

        annonce.incrementViews();
        repo.save(annonce);
    }

    /** Récupère une annonce et compte une vue de plus. */
    @Transactional
    public Annonce findByIdAndIncrementViews(Long id) {
        Annonce annonce = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Annonce non trouvée"));

        annonce.incrementViews();
        return repo.save(annonce);
    }

    public Long countByVendeurId(Long vendeurId) {
        return repo.countByVendeurId(vendeurId);
    }

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

        annonce.addRating(userId, stars);
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

        // Le vendeur ne peut pas noter sa propre annonce
        return annonce.getVendeur() == null || !annonce.getVendeur().getId().equals(userId);
    }

    /**
     * Ajoute une note avec commentaire, ou met à jour la note existante de l'utilisateur.
     * @return false si l'enregistrement a échoué
     */
    public boolean rateAnnonce(Long annonceId, Long userId, Integer stars, String comment) {
        try {
            Annonce annonce = repo.findById(annonceId)
                    .orElseThrow(() -> new RuntimeException("Annonce non trouvée"));

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

            Optional<Rating> existingRating = ratingRepository.findByAnnonceIdAndUserId(annonceId, userId);

            if (existingRating.isPresent()) {
                Rating rating = existingRating.get();
                rating.setStars(stars);
                rating.setComment(comment);
                ratingRepository.save(rating);
                return true;
            } else {
                Rating rating = new Rating(annonce, user, stars, comment);
                ratingRepository.save(rating);
                return true;
            }
        } catch (Exception e) {
            log.error("Échec de l'enregistrement de la note (annonce {}, utilisateur {})", annonceId, userId, e);
            return false;
        }
    }

    public boolean removeRating(Long annonceId, Long userId) {
        try {
            ratingRepository.deleteByAnnonceIdAndUserId(annonceId, userId);
            return true;
        } catch (Exception e) {
            log.error("Échec de la suppression de la note (annonce {}, utilisateur {})", annonceId, userId, e);
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


