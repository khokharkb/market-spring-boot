package org.example.market.favoris;

import jakarta.persistence.*;
import org.example.market.annonce.Annonce;
import org.example.market.user.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "favoris", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "annonce_id"})
})
public class Favori {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "annonce_id", nullable = false)
    private Annonce annonce;

    @Column(name = "date_ajout")
    private LocalDateTime dateAjout;

    // === CONSTRUCTEURS ===

    // Constructeur sans paramètres (requis par JPA)
    public Favori() {
        this.dateAjout = LocalDateTime.now();
    }

    // Constructeur avec User et Annonce
    public Favori(User user, Annonce annonce) {
        this.user = user;
        this.annonce = annonce;
        this.dateAjout = LocalDateTime.now();
    }

    // Constructeur avec tous les champs
    public Favori(Long id, User user, Annonce annonce, LocalDateTime dateAjout) {
        this.id = id;
        this.user = user;
        this.annonce = annonce;
        this.dateAjout = (dateAjout != null) ? dateAjout : LocalDateTime.now();
    }

    // === BUILDER MANUEL ===

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private User user;
        private Annonce annonce;
        private LocalDateTime dateAjout;

        private Builder() {}

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder user(User user) {
            this.user = user;
            return this;
        }

        public Builder annonce(Annonce annonce) {
            this.annonce = annonce;
            return this;
        }

        public Builder dateAjout(LocalDateTime dateAjout) {
            this.dateAjout = dateAjout;
            return this;
        }

        public Favori build() {
            return new Favori(id, user, annonce, dateAjout);
        }
    }

    // === GETTERS ET SETTERS ===

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Annonce getAnnonce() {
        return annonce;
    }

    public void setAnnonce(Annonce annonce) {
        this.annonce = annonce;
    }

    public LocalDateTime getDateAjout() {
        return dateAjout;
    }

    public void setDateAjout(LocalDateTime dateAjout) {
        this.dateAjout = dateAjout;
    }
}