package org.example.market.panier;

import org.example.market.panier.Panier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface PanierRepository extends JpaRepository<Panier, Long> {

    // Trouver tous les articles du panier d'un utilisateur
    @Query("SELECT p FROM Panier p WHERE p.user.id = :userId")
    List<Panier> findByUserId(@Param("userId") Long userId);

    // Trouver un article spécifique dans le panier d'un utilisateur
    @Query("SELECT p FROM Panier p WHERE p.user.id = :userId AND p.annonce.id = :annonceId")
    Optional<Panier> findByUserIdAndAnnonceId(@Param("userId") Long userId,
                                              @Param("annonceId") Long annonceId);

    // Supprimer un article du panier d'un utilisateur
    void deleteByUserIdAndAnnonceId(Long userId, Long annonceId);

    // Supprimer tous les articles du panier d'un utilisateur (vider le panier)
    void deleteByUserId(Long userId);
}