package org.example.market.annonce;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AnnonceRepository extends JpaRepository<Annonce, Long> {

    @Query("SELECT a FROM Annonce a WHERE a.vendeur.username = :username")
    List<Annonce> findByVendeurUsername(@Param("username") String username);
    int countByVendeurUsername(String username);
    List<Annonce> findByVendeurId(Long vendeurId);
    void deleteByVendeurId(Long userId);
    // Trouver les annonces par titre (recherche)
    List<Annonce> findByTitreContainingIgnoreCase(String titre);

    // Trouver les annonces par catégorie
    List<Annonce> findByCategorie(String categorie);

    // Trouver les annonces par titre et catégorie
    List<Annonce> findByTitreContainingIgnoreCaseAndCategorie(String titre, String categorie);

    // Compter les vendeurs distincts
    @Query("SELECT COUNT(DISTINCT a.vendeur) FROM Annonce a")
    Long countDistinctVendeurs();

    // Récupérer toutes les catégories distinctes
    @Query("SELECT DISTINCT a.categorie FROM Annonce a WHERE a.categorie IS NOT NULL")
    List<String> findAllDistinctCategories();
    Long countByVendeurId(Long vendeurId);

    Long countByCategorie(String categorie);
}
