package com.chirp.backend.repository;

import com.chirp.backend.model.AkunUser;
import com.chirp.backend.model.DirectMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface DirectMessageRepository extends JpaRepository<DirectMessage, Long> {

    @Query("SELECT d FROM DirectMessage d WHERE (d.user1 = :u1 AND d.user2 = :u2) OR (d.user1 = :u2 AND d.user2 = :u1)")
    Optional<DirectMessage> findConversation(@Param("u1") AkunUser u1, @Param("u2") AkunUser u2);

    @Query("SELECT d FROM DirectMessage d WHERE d.user1.username = :username OR d.user2.username = :username")
    List<DirectMessage> findAllUserConversations(@Param("username") String username);
}
