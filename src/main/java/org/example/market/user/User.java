package org.example.market.user;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.example.market.annonce.Annonce;
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le nom d'utilisateur est obligatoire")
    @Size(min = 3, max = 50, message = "Le nom d'utilisateur doit contenir entre 3 et 50 caractères")
    @Column(unique = true, nullable = false)
    private String username;

    @NotBlank(message = "Le mot de passe est obligatoire")
    private String password;

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Format d'email invalide")
    @Column(unique = true, nullable = false)
    private String email;

    @Column(name = "nom")
    private String nom;

    @Column(name = "prenom") // Note: le nom de la colonne est "prenim" dans la table
    private String prenom;

    @Pattern(regexp = "^(0|\\+213)[567]\\d{8}$", message = "Numéro de téléphone algérien invalide")
    @Column(name = "telephone") // Note: le nom de la colonne est "tlephone" dans la table
    private String telephone;

    @Column(name = "adresse")
    private String adresse;

    @Column(name = "ville")
    private String ville;

    @Column(name = "`date insription`")
    private LocalDateTime dateInscription = LocalDateTime.now();

    @Column(name = "active")
    private boolean active = true;

    @Column(name = "role")
    private String role = "USER";

    // Relation avec les annonces
    @OneToMany(mappedBy = "vendeur", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Annonce> annonces = new ArrayList<>();

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }

    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) { this.adresse = adresse; }

    public String getVille() { return ville; }
    public void setVille(String ville) { this.ville = ville; }

    public LocalDateTime getDateInscription() { return dateInscription; }
    public void setDateInscription(LocalDateTime dateInscription) { this.dateInscription = dateInscription; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public List<Annonce> getAnnonces() { return annonces; }
    public void setAnnonces(List<Annonce> annonces) { this.annonces = annonces; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    // Méthode utilitaire pour obtenir le nom complet
    public String getFullName() {
        if (prenom != null && nom != null) {
            return prenom + " " + nom;
        } else if (prenom != null) {
            return prenom;
        } else if (nom != null) {
            return nom;
        } else {
            return username;
        }
    }

    // Vérifier si l'utilisateur est admin
    public boolean isAdmin() {
        return "ADMIN".equals(role);
    }
}