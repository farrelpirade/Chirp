package com.chirp.backend.model;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "news")
public class News implements Ketik {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String judul;

    @Lob
    @Column(length = 5000)
    private String konten;

    @Lob
    @Column(length = 1000)
    private String deskripsi;

    @Temporal(TemporalType.TIMESTAMP)
    private Date tanggal;

    public News() {
        this.tanggal = new Date();
    }

    public String getJudul() {
        return this.judul;
    }

    public String getKonten() {
        return this.konten;
    }

    public String getDeskripsi() {
        return this.deskripsi;
    }

    public void setJudul(String judul) {
        this.judul = judul;
    }

    public void setKonten(String konten) {
        this.konten = konten;
    }

    protected Date getTanggal() {
        return this.tanggal;
    }

    protected void Posting() {
        // Logic for posting news can be handled in service, this method satisfies class diagram
    }

    @Override
    public String typing() {
        return "System is generating news summarization...";
    }

    // Standard getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setDeskripsi(String deskripsi) {
        this.deskripsi = deskripsi;
    }

    public void setTanggal(Date tanggal) {
        this.tanggal = tanggal;
    }

    public Date getTanggalPublic() {
        return this.tanggal;
    }
}
