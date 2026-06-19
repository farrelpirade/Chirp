package com.chirp.backend.service;

import com.chirp.backend.model.AkunUser;
import com.chirp.backend.model.DirectMessage;
import com.chirp.backend.model.Message;
import com.chirp.backend.repository.AkunUserRepository;
import com.chirp.backend.repository.DirectMessageRepository;
import com.chirp.backend.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class DirectMessageService {

    private final DirectMessageRepository dmRepository;
    private final MessageRepository messageRepository;
    private final AkunUserRepository userRepository;

    @Autowired
    public DirectMessageService(DirectMessageRepository dmRepository,
                                MessageRepository messageRepository,
                                AkunUserRepository userRepository) {
        this.dmRepository = dmRepository;
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
    }

    public DirectMessage getOrCreateConversation(String username1, String username2) {
        AkunUser u1 = userRepository.findById(username1)
                .orElseThrow(() -> new IllegalArgumentException("User 1 not found"));
        AkunUser u2 = userRepository.findById(username2)
                .orElseThrow(() -> new IllegalArgumentException("User 2 not found"));

        Optional<DirectMessage> existing = dmRepository.findConversation(u1, u2);
        if (existing.isPresent()) {
            return existing.get();
        }

        DirectMessage newDm = new DirectMessage(u1, u2);
        return dmRepository.save(newDm);
    }

    public Message sendMessage(Long conversationId, String senderUsername, String text) {
        DirectMessage dm = dmRepository.findById(conversationId)
                .orElseThrow(() -> new IllegalArgumentException("Conversation not found"));

        Message msg = new Message(senderUsername, text);
        Message savedMsg = messageRepository.save(msg);

        dm.addMessage(savedMsg);
        dmRepository.save(dm);
        
        return savedMsg;
    }

    public List<DirectMessage> getConversations(String username) {
        return dmRepository.findAllUserConversations(username);
    }

    public DirectMessage pinMessage(Long conversationId, Long messageId) {
        DirectMessage dm = dmRepository.findById(conversationId)
                .orElseThrow(() -> new IllegalArgumentException("Conversation not found"));

        dm.pinMessage(messageId);
        return dmRepository.save(dm);
    }

    public DirectMessage deleteMessage(Long conversationId, Long messageId) {
        DirectMessage dm = dmRepository.findById(conversationId)
                .orElseThrow(() -> new IllegalArgumentException("Conversation not found"));

        dm.deleteMessage(messageId);
        return dmRepository.save(dm);
    }

    public DirectMessage getConversation(Long conversationId) {
        return dmRepository.findById(conversationId)
                .orElseThrow(() -> new IllegalArgumentException("Conversation not found"));
    }

    public DirectMessage sortMessages(Long conversationId) {
        DirectMessage dm = dmRepository.findById(conversationId)
                .orElseThrow(() -> new IllegalArgumentException("Conversation not found"));

        dm.sortMessage();
        return dmRepository.save(dm);
    }
}
