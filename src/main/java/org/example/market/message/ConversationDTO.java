package org.example.market.message;

public class ConversationDTO {
    private Long conversationId;
    private Long annonceId;
    private String annonceTitle;
    private String otherUser;
    private String lastMessage;
    private String lastTime;
    private int unreadCount;

    public ConversationDTO(Long conversationId, Long annonceId, String annonceTitle,
                           String otherUser, String lastMessage,
                           String lastTime, int unreadCount) {
        this.conversationId = conversationId;
        this.annonceId = annonceId;
        this.annonceTitle = annonceTitle;
        this.otherUser = otherUser;
        this.lastMessage = lastMessage;
        this.lastTime = lastTime;
        this.unreadCount = unreadCount;
    }
    public Long getAnnonceId() {
        return annonceId;
    }
    public void setAnnonceId(Long annonceId) {
        this.annonceId = annonceId;
    }
    public String getAnnonceTitle() {
        return annonceTitle;
    }
    public void setAnnonceTitle(String annonceTitle) {
        this.annonceTitle = annonceTitle;
    }

    public Long getConversationId() {
        return conversationId;
    }

    public void setConversationId(Long conversationId) {
        this.conversationId = conversationId;
    }

    public String getOtherUserUsername() {
        return otherUser;
    }
    public void setOtherUserUsername(String otherUserUsername) {
        this.otherUser = otherUserUsername;
    }
    public String getLastMessage() {
        return lastMessage;
    }
    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }
    public String getLastMessageTime() {
        return lastTime;
    }
    public void setLastMessageTime(String lastMessageTime) {
        this.lastTime = lastMessageTime;
    }
    public int getUnreadCount() {
        return unreadCount;
    }
    public void setUnreadCount(int unreadCount) {
        this.unreadCount = unreadCount;
    }

}

