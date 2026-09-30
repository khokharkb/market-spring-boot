package org.example.market.rating;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface RatingRepository extends JpaRepository<Rating, Long> {

    // Trouver une note spécifique
    Optional<Rating> findByAnnonceIdAndUserId(Long annonceId, Long userId);

    // Vérifier si une note existe
    boolean existsByAnnonceIdAndUserId(Long annonceId, Long userId);

    // Trouver toutes les notes d'une annonce
    List<Rating> findByAnnonceId(Long annonceId);

    // Calculer la moyenne
    @Query("SELECT AVG(r.stars) FROM Rating r WHERE r.annonce.id = :annonceId")
    Double findAverageRatingByAnnonceId(@Param("annonceId") Long annonceId);

    // Compter le nombre de notes
    Long countByAnnonceId(Long annonceId);

    // Supprimer une note
    void deleteByAnnonceIdAndUserId(Long annonceId, Long userId);
}
