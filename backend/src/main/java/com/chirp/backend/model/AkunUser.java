package com.chirp.backend.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "akun_user")
public class AkunUser {

    @Id
    public String username;
    public String fullName;
    private String NomorTelpon;
    private String email;
    private String password;

    // No-arg constructor required by JPA
    public AkunUser() {}

    // Constructor specified in Class Diagram
    public AkunUser(String username, String password, String email, String NomorTelpon, String fullName) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.NomorTelpon = NomorTelpon;
        this.fullName = fullName;
    }

    public boolean login(String username, String password) {
        return this.username.equals(username) && this.password.equals(password);
    }

    public void logout() {
        // Logout logic can be handled at session level, this method satisfies class diagram
    }

    public void changeUsername(String newUsername) {
        this.username = newUsername;
    }

    public void changeFullName(String newFullName) {
        this.fullName = newFullName;
    }

    public void changePassword(String newPassword) {
        this.password = newPassword;
    }

    // Standard getters and setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getNomorTelpon() {
        return NomorTelpon;
    }

    public void setNomorTelpon(String nomorTelpon) {
        this.NomorTelpon = nomorTelpon;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
