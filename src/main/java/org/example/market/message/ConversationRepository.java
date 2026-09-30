package org.example.market.message;

import org.example.market.annonce.Annonce;
import org.example.market.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    Optional<Conversation> findByAnnonceAndBuyer(Annonce annonce, User buyer);
    List<Conversation> findByBuyerOrSeller(User buyer, User seller);
    Optional<Conversation> findByAnnonce(Annonce annonce);
}