package org.example.market.message;

import org.example.market.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByConversationIdOrderBySentAtAsc(Long conversationId);
    List<Message> findByReceiverOrderBySentAtDesc(User receiver);
    List<Message> findBySenderOrderBySentAtDesc(User sender);
    List<Message> findBySenderOrReceiverOrderBySentAtDesc(User sender, User receiver);
    int countByConversationIdAndReceiverAndReadFalse(Long conversationId, User receiver);

    long countByReceiverAndReadFalse(User receiver);
    List<Message> findByConversation(Conversation conversation);
    List<Message> findByConversationIdAndReceiverUsernameAndReadFalse(Long conversationId, String username);

}