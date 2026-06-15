package com.chirp.backend.repository;

import com.chirp.backend.model.ChatbotMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ChatbotMessageRepository extends JpaRepository<ChatbotMessage, Long> {
    List<ChatbotMessage> findByUsernameOrderByIdAsc(String username);
    void deleteByUsername(String username);
}
