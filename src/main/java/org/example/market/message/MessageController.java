package org.example.market.message;

import lombok.extern.slf4j.Slf4j;
import org.example.market.annonce.Annonce;
import org.example.market.annonce.AnnonceService;
import org.example.market.user.User;
import org.example.market.user.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;

@Slf4j
@Controller
@CrossOrigin(origins = {"http://localhost:3000", "capacitor://localhost", "http://localhost"})
@RequestMapping("/messages")
public class MessageController {

    private final MessageService messageService;
    private final AnnonceService annonceService;
    private final UserService userService;

    public MessageController(MessageService messageService, AnnonceService annonceService, UserService userService) {
        this.messageService = messageService;
        this.annonceService = annonceService;
        this.userService = userService;
    }

    // Page principale : liste des conversations
    @GetMapping
    public String messagesPage(Principal principal, Model model) {
        String username = principal.getName();
        List<ConversationDTO> conversations = messageService.getConversations(username);
        model.addAttribute("conversations", conversations);
        model.addAttribute("view", "conversations");
        model.addAttribute("currentUser", username);
        return "messages";
    }

    @GetMapping("/conversation/{conversationId}")
    public String viewConversation(@PathVariable Long conversationId,
                                   Principal principal,
                                   Model model) {
        String username = principal.getName();

        // Ouvrir la conversation marque ses messages comme lus
        messageService.markMessagesAsRead(conversationId, username);

        List<Message> messages = messageService.getMessages(conversationId);
        model.addAttribute("messages", messages);

        // Toutes les conversations, pour la barre latérale
        List<ConversationDTO> conversations = messageService.getConversations(username);
        model.addAttribute("conversations", conversations);

        model.addAttribute("selectedConversationId", conversationId);
        model.addAttribute("view", "single-conversation");

        // Infos de l'annonce liée à la conversation
        ConversationDTO selectedConv = conversations.stream()
                .filter(c -> c.getConversationId().equals(conversationId))
                .findFirst()
                .orElse(null);

        if (selectedConv != null) {
            Annonce annonce = annonceService.findById(selectedConv.getAnnonceId());
            model.addAttribute("annonce", annonce);
            model.addAttribute("annonceId", selectedConv.getAnnonceId());
            model.addAttribute("annonceTitle", selectedConv.getAnnonceTitle());
            model.addAttribute("otherUser", selectedConv.getOtherUserUsername());
        }

        model.addAttribute("currentUser", username);

        return "messages";
    }


    // Messages reçus
    @GetMapping("/inbox")
    public String getInbox(Principal principal, Model model) {
        String username = principal.getName();
        model.addAttribute("messages", messageService.getInbox(username));
        model.addAttribute("view", "inbox");
        model.addAttribute("conversations", messageService.getConversations(username));
        model.addAttribute("currentUser", username);
        return "messages";
    }

    // Messages envoyés
    @GetMapping("/sent")
    public String getSentMessages(Principal principal, Model model) {
        String username = principal.getName();
        model.addAttribute("messages", messageService.getSentMessages(username));
        model.addAttribute("view", "sent");
        model.addAttribute("conversations", messageService.getConversations(username));
        model.addAttribute("currentUser", username);
        return "messages";
    }

    // Tous les messages
    @GetMapping("/all")
    public String getAllMessages(Principal principal, Model model) {
        String username = principal.getName();
        model.addAttribute("messages", messageService.getAllMessagesForUser(username));
        model.addAttribute("view", "all");
        model.addAttribute("conversations", messageService.getConversations(username));
        model.addAttribute("currentUser", username);
        return "messages";
    }

    @PostMapping("/send")
    public String sendMessage(@RequestParam Long conversationId,
                              @RequestParam(required = false) Long annonceId,
                              @RequestParam String messageContent,
                              Principal principal,
                              RedirectAttributes redirectAttributes) {

        try {
            String username = principal.getName();

            Long actualAnnonceId = annonceId;

            // Si l'annonce n'est pas fournie, on la retrouve via la conversation
            if (actualAnnonceId == null) {
                List<ConversationDTO> conversations = messageService.getConversations(username);
                ConversationDTO conversation = conversations.stream()
                        .filter(c -> c.getConversationId().equals(conversationId))
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException("Conversation not found"));
                actualAnnonceId = conversation.getAnnonceId();
            }

            messageService.sendMessage(actualAnnonceId, messageContent, username);

            redirectAttributes.addFlashAttribute("successMessage", "Message envoyé avec succès!");

        } catch (Exception e) {
            log.error("Échec de l'envoi du message (conversation {})", conversationId, e);
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Erreur lors de l'envoi du message: " + e.getMessage());
        }

        return "redirect:/messages/conversation/" + conversationId;
    }
}