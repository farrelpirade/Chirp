package com.chirp.backend.repository;

import com.chirp.backend.model.Thread;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ThreadRepository extends JpaRepository<Thread, Long> {
    List<Thread> findAllByOrderByTanggalDesc();
    List<Thread> findByUser_UsernameOrderByTanggalDesc(String username);
    List<Thread> findByKontenContainingIgnoreCaseOrderByTanggalDesc(String keyword);

    @Query("SELECT t FROM Thread t JOIN t.reply r WHERE r.id = :replyId")
    Optional<Thread> findByReplyId(@Param("replyId") Long replyId);
}
