package org.example.market.message;

import jakarta.persistence.*;
import org.example.market.annonce.Annonce;
import org.example.market.user.User;

import java.time.LocalDateTime;

@Entity
public class Conversation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Annonce annonce;

    @ManyToOne
    private User buyer;

    @ManyToOne
    private User seller;

    private LocalDateTime lastUpdated;

    // getters & setters
    public Long getId() { return id; }

    public Annonce getAnnonce() { return annonce; }
    public void setAnnonce(Annonce annonce) { this.annonce = annonce; }

    public User getBuyer() { return buyer; }
    public void setBuyer(User buyer) { this.buyer = buyer; }

    public User getSeller() { return seller; }
    public void setSeller(User seller) { this.seller = seller; }

    public LocalDateTime getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }
}
