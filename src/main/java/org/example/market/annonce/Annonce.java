package org.example.market.annonce;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.example.market.user.User;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Entity
@Table(name = "annonce")  // Ajoute cette annotation
public class Annonce {
    @Column(name = "view_count")
    private Integer viewCount = 0;

    @ElementCollection
    @CollectionTable(name = "annonce_viewers", joinColumns = @JoinColumn(name = "annonce_id"))
    @Column(name = "viewer_id")
    private Set<String> viewerIds = new HashSet<>();

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titre;

    @Column(length = 2000)
    private String description;

    private Double prix;

    private String categorie;

    private String telephone;

    private String imagePath;

    private LocalDateTime createdAt = LocalDateTime.now();

    @ManyToOne
    @JoinColumn(name = "vendeur_id")  // Ajoute ceci pour être explicite
    private User vendeur;

    @Column(name = "views", nullable = false)
    private Integer views = 0;

    // CORRECTION ICI : Déplace l'annotation @JoinColumn
    @ManyToOne
    @JoinColumn(name = "user_id")  // Place-la AVEC @ManyToOne
    private User user;

    // === NOUVEAUX CHAMPS POUR LES NOTES ===

    // Map pour stocker les notes par utilisateur
    @ElementCollection
    @CollectionTable(
            name = "annonce_ratings",
            joinColumns = @JoinColumn(name = "annonce_id")  // Simplifié
    )
    @MapKeyColumn(name = "user_id")
    @Column(name = "rating")
    private Map<Long, Integer> userRatings = new HashMap<>();

    @Column(name = "average_rating")
    private Double averageRating = 0.0;

    @Column(name = "rating_count")
    private Integer ratingCount = 0;

    // === CONSTRUCTEUR ===
    public Annonce() {
        this.userRatings = new HashMap<>();
        this.averageRating = 0.0;
        this.ratingCount = 0;
        this.views = 0;
    }

    public Long getId() {
        return id;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getPrix() {
        return prix;
    }

    public void setPrix(Double prix) {
        this.prix = prix;
    }

    public String getCategorie() {
        return categorie;
    }

    public void setCategorie(String categorie) {
        this.categorie = categorie;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public User getVendeur() {
        return vendeur;
    }

    public void setVendeur(User vendeur) {
        this.vendeur = vendeur;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isOwner(User user) {
        return user != null && vendeur != null && vendeur.getId().equals(user.getId());
    }

    public boolean isOwner(String username) {
        return vendeur != null && vendeur.getUsername().equals(username);
    }

    @NotBlank(message = "Le téléphone est obligatoire")
    @Pattern(regexp = "^(0|\\+213)[567]\\d{8}$",
            message = "Numéro de téléphone algérien invalide")
    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Integer getViews() {
        return views != null ? views : 0;
    }

    public void setViews(Integer views) {
        this.views = views;
    }

    public void incrementViews() {
        if (this.views == null) {
            this.views = 0;
        }
        this.views++;
    }

    public Map<Long, Integer> getUserRatings() {
        if (userRatings == null) {
            userRatings = new HashMap<>();
        }
        return userRatings;
    }

    public void setUserRatings(Map<Long, Integer> userRatings) {
        this.userRatings = userRatings;
        recalculateRating();
    }

    public Double getAverageRating() {
        return averageRating != null ? averageRating : 0.0;
    }

    public void setAverageRating(Double averageRating) {
        this.averageRating = averageRating;
    }

    public Integer getRatingCount() {
        return ratingCount != null ? ratingCount : 0;
    }

    public void setRatingCount(Integer ratingCount) {
        this.ratingCount = ratingCount;
    }

    // === MÉTHODES UTILITAIRES POUR LES NOTES ===

    public void addRating(Long userId, Integer stars) {
        if (stars < 1 || stars > 5) {
            throw new IllegalArgumentException("La note doit être entre 1 et 5 étoiles");
        }

        if (userRatings == null) {
            userRatings = new HashMap<>();
        }

        userRatings.put(userId, stars);
        recalculateRating();
    }

    public void removeRating(Long userId) {
        if (userRatings != null && userRatings.containsKey(userId)) {
            userRatings.remove(userId);
            recalculateRating();
        }
    }

    public Integer getUserRating(Long userId) {
        if (userRatings == null) return null;
        return userRatings.get(userId);
    }

    public boolean hasUserRated(Long userId) {
        if (userRatings == null) return false;
        return userRatings.containsKey(userId);
    }

    private void recalculateRating() {
        if (userRatings == null || userRatings.isEmpty()) {
            averageRating = 0.0;
            ratingCount = 0;
            return;
        }

        int sum = 0;
        for (Integer rating : userRatings.values()) {
            sum += rating;
        }

        ratingCount = userRatings.size();
        averageRating = Math.round((sum * 10.0) / ratingCount) / 10.0;
    }

    public String getFormattedRating() {
        return String.format("%.1f", getAverageRating());
    }

    public int getStarPercentage(int starNumber) {
        Double avg = getAverageRating();
        if (avg >= starNumber) {
            return 100;
        } else if (avg >= starNumber - 0.5) {
            return 50;
        } else {
            return 0;
        }
    }
}