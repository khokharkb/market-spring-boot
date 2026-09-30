package org.example.market.message;

import org.example.market.annonce.Annonce;
import org.example.market.annonce.AnnonceRepository;
import org.example.market.user.User;
import org.example.market.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MessageService {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private AnnonceRepository annonceRepository;


    public void sendMessage(Long annonceId, String content, String senderUsername) {
        User sender = userService.findByUsername(senderUsername);
        Annonce annonce = annonceRepository.findById(annonceId).orElseThrow();
        User seller = annonce.getVendeur();

        // Find or create conversation
        Conversation conversation = conversationRepository
                .findByAnnonce(annonce) // Try to find any conversation for this annonce
                .orElseGet(() -> {
                    // If no conversation exists, create a new one
                    // Only buyers should be able to start conversations
                    if (sender.equals(seller)) {
                        throw new IllegalStateException("Seller cannot start a conversation with themselves");
                    }

                    Conversation c = new Conversation();
                    c.setAnnonce(annonce);
                    c.setBuyer(sender);  // The person starting the conversation is the buyer
                    c.setSeller(seller);  // The annonce owner is the seller
                    c.setLastUpdated(LocalDateTime.now());
                    return conversationRepository.save(c);
                });

        // Now we have a conversation, determine who the receiver should be
        // If sender is buyer, receiver should be seller
        // If sender is seller, receiver should be buyer
        User receiver;

        if (sender.equals(conversation.getBuyer())) {
            // Sender is the buyer, so receiver is the seller
            receiver = conversation.getSeller();
        } else if (sender.equals(conversation.getSeller())) {
            // Sender is the seller, so receiver is the buyer
            receiver = conversation.getBuyer();
        } else {
            // This shouldn't happen - sender is neither buyer nor seller in this conversation
            throw new IllegalStateException("You are not part of this conversation");
        }

        Message msg = new Message();
        msg.setConversation(conversation);
        msg.setSender(sender);
        msg.setReceiver(receiver);
        msg.setContent(content);
        msg.setSentAt(LocalDateTime.now());
        msg.setRead(false);

        messageRepository.save(msg);
    }

    public List<Message> getMessages(Long conversationId) {
        return messageRepository.findByConversationIdOrderBySentAtAsc(conversationId);
    }

    public List<ConversationDTO> getConversations(String username) {
        User user = userService.findByUsername(username);
        List<Conversation> conversations =
                conversationRepository.findByBuyerOrSeller(user, user);

        return conversations.stream().map(c -> {
            boolean isBuyer = c.getBuyer().equals(user);
            String otherUser = isBuyer
                    ? c.getSeller().getUsername()
                    : c.getBuyer().getUsername();

            List<Message> msgs =
                    messageRepository.findByConversationIdOrderBySentAtAsc(c.getId());

            Message last = msgs.isEmpty() ? null : msgs.get(msgs.size() - 1);

            int unread = (int) messageRepository
                    .countByConversationIdAndReceiverAndReadFalse(c.getId(), user);

            return new ConversationDTO(
                    c.getId(),
                    c.getAnnonce().getId(),
                    c.getAnnonce().getTitre(),
                    otherUser,
                    last != null ? last.getContent() : "",
                    last != null ? last.getSentAt().toString() : "",
                    unread
            );
        }).toList();
    }

    // Add these missing methods:
    public List<Message> getInbox(String username) {
        User user = userService.findByUsername(username);
        return messageRepository.findByReceiverOrderBySentAtDesc(user);
    }

    public List<Message> getSentMessages(String username) {
        User user = userService.findByUsername(username);
        return messageRepository.findBySenderOrderBySentAtDesc(user);
    }

    public List<Message> getAllMessagesForUser(String username) {
        User user = userService.findByUsername(username);
        return messageRepository.findBySenderOrReceiverOrderBySentAtDesc(user, user);
    }

    public List<Message> getConversationMessages(Long annonceId, String username) {
        User user = userService.findByUsername(username);
        Annonce annonce = annonceRepository.findById(annonceId).orElseThrow();

        return conversationRepository.findByAnnonceAndBuyer(annonce, user)
                .map(conversation -> messageRepository.findByConversationIdOrderBySentAtAsc(conversation.getId()))
                .orElseGet(List::of);
    }

    public String getOtherUserInConversation(Long annonceId, String username) {
        User user = userService.findByUsername(username);
        Annonce annonce = annonceRepository.findById(annonceId).orElseThrow();

        return conversationRepository.findByAnnonceAndBuyer(annonce, user)
                .map(conversation -> {
                    if (conversation.getBuyer().equals(user)) {
                        return conversation.getSeller().getUsername();
                    } else {
                        return conversation.getBuyer().getUsername();
                    }
                })
                .orElse(null);
    }
    /**
         * Count unread messages for a user
         */
        public long countUnreadMessages(String username) {
            User user = userService.findByUsername(username);
            return messageRepository.countByReceiverAndReadFalse(user);
        }

        /**
         * Get recent messages for a user (limit by count)
         */
        public List<Message> getRecentMessages(String username, int limit) {
            User user = userService.findByUsername(username);
            // First get all messages for the user
            List<Message> allMessages = messageRepository.findBySenderOrReceiverOrderBySentAtDesc(user, user);

            // Return only the specified number of recent messages
            return allMessages.stream()
                    .limit(limit)
                    .collect(Collectors.toList());
        }
    public void markMessagesAsRead(Long conversationId, String username) {
        List<Message> unreadMessages = messageRepository.findByConversationIdAndReceiverUsernameAndReadFalse(conversationId, username);
        for (Message m : unreadMessages) {
            m.setRead(true);
        }
        messageRepository.saveAll(unreadMessages);
    }

    }

