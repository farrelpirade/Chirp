package com.chirp.backend.model;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "message")
public class Message implements Ketik {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username; // Sender's username

    @Temporal(TemporalType.TIMESTAMP)
    private Date tanggalKirim;

    @Column(length = 2000)
    private String teks;

    private boolean pinned;

    public Message() {
        this.tanggalKirim = new Date();
    }

    public Message(String username, String teks) {
        this.username = username;
        this.teks = teks;
        this.tanggalKirim = new Date();
    }

    public void kirimMessage() {}

    public void hapusMessage() {}

    @Override
    public String typing() {
        return username + " is typing a message...";
    }

    // Standard getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Date getTanggalKirim() {
        return tanggalKirim;
    }

    public void setTanggalKirim(Date tanggalKirim) {
        this.tanggalKirim = tanggalKirim;
    }

    public String getTeks() {
        return teks;
    }

    public void setTeks(String teks) {
        this.teks = teks;
    }

    public boolean isPinned() {
        return pinned;
    }

    public void setPinned(boolean pinned) {
        this.pinned = pinned;
    }
}
