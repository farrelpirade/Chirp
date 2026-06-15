package com.chirp.backend.model;

import jakarta.persistence.*;

@Entity
@Table(name = "chatbot_message")
public class ChatbotMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String role; // "user" or "model" / "assistant"

    @Column(length = 4000)
    private String kontent; // Spelled kontent in the class diagram

    private String username; // Associate with a user

    public ChatbotMessage() {}

    public ChatbotMessage(String role, String kontent) {
        this.role = role;
        this.kontent = kontent;
    }

    public ChatbotMessage(String role, String kontent, String username) {
        this.role = role;
        this.kontent = kontent;
        this.username = username;
    }

    public String getMessage() {
        return this.kontent;
    }

    public String getRole() {
        return this.role;
    }

    public String setRole(String role) {
        this.role = role;
        return this.role;
    }

    public String setRole() {
        return this.role;
    }

    public String setMessage(String message) {
        this.kontent = message;
        return this.kontent;
    }

    public String setMessage() {
        return this.kontent;
    }

    // Standard getters and setters for fields
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getKontent() {
        return kontent;
    }

    public void setKontent(String kontent) {
        this.kontent = kontent;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
