
package org.example.market.panier;

import jakarta.persistence.*;
import org.example.market.annonce.Annonce;
import org.example.market.user.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "panier")
public class Panier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "annonce_id", nullable = false)
    private Annonce annonce;

    @Column(name = "quantite")
    private Integer quantite = 1;

    @Column(name = "date_ajout")
    private LocalDateTime dateAjout = LocalDateTime.now();

    // Constructeur vide requis par JPA
    public Panier() {
    }

    public Panier(User user, Annonce annonce) {
        this.user = user;
        this.annonce = annonce;
    }

    public Panier(User user, Annonce annonce, Integer quantite) {
        this.user = user;
        this.annonce = annonce;
        this.quantite = quantite;
    }

    // Getters

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public Annonce getAnnonce() {
        return annonce;
    }

    public Integer getQuantite() {
        return quantite;
    }

    public LocalDateTime getDateAjout() {
        return dateAjout;
    }

    // Setters

    public void setId(Long id) {
        this.id = id;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setAnnonce(Annonce annonce) {
        this.annonce = annonce;
    }

    public void setQuantite(Integer quantite) {
        this.quantite = quantite;
    }

    public void setDateAjout(LocalDateTime dateAjout) {
        this.dateAjout = dateAjout;
    }

    // Gestion de la quantité

    public void incrementerQuantite() {
        this.quantite++;
    }

    public void decrementerQuantite() {
        if (this.quantite > 1) {
            this.quantite--;
        }
    }

    public Double getSousTotal() {
        if (annonce != null && annonce.getPrix() != null && quantite != null) {
            return annonce.getPrix() * quantite;
        }
        return 0.0;
    }

    // equals, hashCode, toString

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Panier panier = (Panier) o;
        return id != null && id.equals(panier.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Panier{" +
                "id=" + id +
                ", user=" + (user != null ? user.getId() : "null") +
                ", annonce=" + (annonce != null ? annonce.getId() : "null") +
                ", quantite=" + quantite +
                '}';
    }
}