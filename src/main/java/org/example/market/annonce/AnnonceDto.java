/*package org.example.market.annonce;

import lombok.Data;
import org.example.market.user.UserDTO;

import java.time.LocalDateTime;

@Data
public class AnnonceDto {
    private Long id;
    private String titre;
    private String description;
    private Double prix;
    private String categorie;
    private String imageUrl;
    private Integer views;
    private LocalDateTime createdAt;
    private String telephone;
    private UserDTO vendeur;

    public static AnnonceDto fromEntity(Annonce annonce) {
        AnnonceDto dto = new AnnonceDto();
        dto.setId(annonce.getId());
        dto.setTitre(annonce.getTitre());
        dto.setDescription(annonce.getDescription());
        dto.setPrix(annonce.getPrix());
        dto.setCategorie(annonce.getCategorie());
        dto.setViews(annonce.getViews());
        dto.setCreatedAt(annonce.getCreatedAt());
        dto.setTelephone(annonce.getTelephone());

        // URL complète de l'image
        dto.setImageUrl("/uploads/" + annonce.getImagePath());

        // Vendeur
        if (annonce.getVendeur() != null) {
            dto.setVendeur(UserDTO.fromEntity(annonce.getVendeur()));
        }

        return dto;
    }
}
*/