package com.chirp.backend.repository;

import com.chirp.backend.model.Bookmark;
import com.chirp.backend.model.Thread;
import com.chirp.backend.model.AkunUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {
    List<Bookmark> findAllByUserOrderByThread_TanggalDesc(AkunUser user);
    Optional<Bookmark> findByUserAndThread(AkunUser user, Thread thread);
    void deleteByUserAndThread(AkunUser user, Thread thread);
    void deleteByThread(Thread thread);
}
