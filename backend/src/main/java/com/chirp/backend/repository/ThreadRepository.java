package com.chirp.backend.repository;

import com.chirp.backend.model.Thread;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ThreadRepository extends JpaRepository<Thread, Long> {
    List<Thread> findAllByOrderByTanggalDesc();
    List<Thread> findByUser_UsernameOrderByTanggalDesc(String username);
    List<Thread> findByKontenContainingIgnoreCaseOrderByTanggalDesc(String keyword);
}
