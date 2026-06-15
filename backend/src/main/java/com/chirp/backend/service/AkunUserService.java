package com.chirp.backend.service;

import com.chirp.backend.model.AkunUser;
import com.chirp.backend.repository.AkunUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class AkunUserService {

    private final AkunUserRepository userRepository;

    @Autowired
    public AkunUserService(AkunUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public AkunUser register(String username, String password, String email, String nomorTelpon, String fullName) {
        if (userRepository.existsById(username)) {
            throw new IllegalArgumentException("Username already exists");
        }
        AkunUser user = new AkunUser(username, password, email, nomorTelpon, fullName);
        return userRepository.save(user);
    }

    public AkunUser login(String username, String password) {
        Optional<AkunUser> optionalUser = userRepository.findById(username);
        if (optionalUser.isPresent()) {
            AkunUser user = optionalUser.get();
            if (user.login(username, password)) {
                return user;
            }
        }
        throw new IllegalArgumentException("Invalid username or password");
    }

    public void logout(String username) {
        // Can perform session cleanup if needed, matches the class diagram's logout feature
    }

    public AkunUser updateProfile(String username, String newFullName, String newNomorTelpon, String newEmail, String newPassword) {
        AkunUser user = userRepository.findById(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (newFullName != null && !newFullName.isEmpty()) {
            user.changeFullName(newFullName);
        }
        if (newNomorTelpon != null && !newNomorTelpon.isEmpty()) {
            user.setNomorTelpon(newNomorTelpon);
        }
        if (newEmail != null && !newEmail.isEmpty()) {
            user.setEmail(newEmail);
        }
        if (newPassword != null && !newPassword.isEmpty()) {
            user.changePassword(newPassword);
        }

        return userRepository.save(user);
    }

    public Optional<AkunUser> findByUsername(String username) {
        return userRepository.findById(username);
    }
}
