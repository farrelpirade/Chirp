package com.chirp.backend.repository;

import com.chirp.backend.model.AkunUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AkunUserRepository extends JpaRepository<AkunUser, String> {
}
