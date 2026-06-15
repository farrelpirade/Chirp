package com.chirp.backend.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "thread_post")
public class Thread implements Ketik {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "like_count")
    private int like;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "thread_id")
    private List<Reply> reply = new ArrayList<>();

    @Column(name = "repost_count")
    private int repost;

    @Column(name = "bookmark_count")
    private int bookmark;

    @ManyToOne
    @JoinColumn(name = "user_username")
    private AkunUser user;

    private String konten;

    @Temporal(TemporalType.TIMESTAMP)
    private Date tanggal;

    public Thread() {
        this.tanggal = new Date();
    }

    // Methods from Class Diagram
    public void setLike(int like) {
        this.like = like;
    }

    public void setUser(AkunUser akun) {
        this.user = akun;
    }

    public void setBookmark(int bookmark) {
        this.bookmark = bookmark;
    }

    public void setReply(Reply reply) {
        if (this.reply == null) {
            this.reply = new ArrayList<>();
        }
        this.reply.add(reply);
    }

    public int getLike() {
        return this.like;
    }

    public void setKonten(String konten) {
        this.konten = konten;
    }

    public AkunUser getUser() {
        return this.user;
    }

    public int getBookmark() {
        return this.bookmark;
    }

    public Reply[] getReply() {
        if (this.reply == null) {
            return new Reply[0];
        }
        return this.reply.toArray(new Reply[0]);
    }

    public int getRepost() {
        return this.repost;
    }

    protected Date getTanggal() {
        return this.tanggal;
    }

    protected void Posting() {
        // Logic for posting thread can be handled in service, this method satisfies class diagram
    }

    protected void Repost() {
        this.repost++;
    }

    @Override
    public String typing() {
        if (user != null) {
            return user.getUsername() + " is typing a post...";
        }
        return "Someone is typing a post...";
    }

    // Standard getters and setters for other fields
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setReply(List<Reply> reply) {
        this.reply = reply;
    }

    public void setRepost(int repost) {
        this.repost = repost;
    }

    public String getKonten() {
        return konten;
    }

    public void setTanggal(Date tanggal) {
        this.tanggal = tanggal;
    }

    public Date getTanggalPublic() {
        return this.tanggal;
    }
}
