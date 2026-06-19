package com.chirp.backend.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Entity
@Table(name = "direct_message")
public class DirectMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user1_username")
    private AkunUser user1;

    @ManyToOne
    @JoinColumn(name = "user2_username")
    private AkunUser user2;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "direct_message_id")
    private List<Message> messages = new ArrayList<>();

    public DirectMessage() {}

    public DirectMessage(AkunUser user1, AkunUser user2) {
        this.user1 = user1;
        this.user2 = user2;
    }

    public List<Message> showMessage() {
        return this.messages;
    }

    public void sortMessage() {
        if (this.messages != null) {
            this.messages.sort(Comparator.comparing(Message::getTanggalKirim));
        }
    }

    public void pinMessage(Long messageId) {
        if (this.messages != null) {
            for (Message m : this.messages) {
                if (m.getId().equals(messageId)) {
                    m.setPinned(!m.isPinned());
                    break;
                }
            }
        }
    }

    public void deleteMessage(Long messageId) {
        if (this.messages != null) {
            this.messages.removeIf(m -> m.getId().equals(messageId));
        }
    }

    public void deleteMessage() {}

    public void pinMessage() {}

    public void addMessage(Message message) {
        if (this.messages == null) {
            this.messages = new ArrayList<>();
        }
        this.messages.add(message);
    }

    public void addMessage() {}

    public String notification() {
        return "New message in conversation between " + user1.getUsername() + " and " + user2.getUsername();
    }

    // Standard getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public AkunUser getUser1() {
        return user1;
    }

    public void setUser1(AkunUser user1) {
        this.user1 = user1;
    }

    public AkunUser getUser2() {
        return user2;
    }

    public void setUser2(AkunUser user2) {
        this.user2 = user2;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }
}
