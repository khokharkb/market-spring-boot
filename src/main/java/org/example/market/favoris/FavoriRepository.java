package org.example.market.favoris;

import org.example.market.annonce.Annonce;
import org.example.market.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriRepository extends JpaRepository<Favori, Long> {

    // Trouver un favori par utilisateur et annonce
    Optional<Favori> findByUserAndAnnonce(User user, Annonce annonce);

    // Vérifier si un favori existe
    boolean existsByUserAndAnnonce(User user, Annonce annonce);

    // Trouver tous les favoris d'un utilisateur
    List<Favori> findByUserOrderByDateAjoutDesc(User user);

    // Supprimer un favori par utilisateur et annonce
    void deleteByUserAndAnnonce(User user, Annonce annonce);
    @Query("SELECT f FROM Favori f JOIN FETCH f.annonce WHERE f.user = :user ORDER BY f.dateAjout DESC")
    List<Favori> findFavorisWithAnnonceByUser(@Param("user") User user);
    Long countByAnnonce(Annonce annonce);
    // Dans FavoriRepository
    @Query("SELECT f.annonce.id FROM Favori f WHERE f.user.id = :userId")
    List<Long> findAnnonceIdsByUserId(@Param("userId") Long userId);

}