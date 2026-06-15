package com.chirp.backend.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "reply")
public class Reply implements Ketik {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "like_count")
    private int like;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "parent_reply_id")
    private List<Reply> reply = new ArrayList<>();

    @Column(name = "bookmark_count")
    private int bookmark;

    @ManyToOne
    @JoinColumn(name = "reply_to_username")
    private AkunUser replyTo;

    @ManyToOne
    @JoinColumn(name = "user_username")
    private AkunUser user;

    private String konten;

    public Reply() {}

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

    public int getLike() {
        return this.like;
    }

    // Return the first reply or null, matching the exact return type in diagram (+ getReply(): Reply)
    public Reply getReply() {
        if (reply != null && !reply.isEmpty()) {
            return reply.get(0);
        }
        return null;
    }

    // A helper method to get all replies if needed
    public List<Reply> getReplyList() {
        return this.reply;
    }

    public AkunUser getUser() {
        return this.user;
    }

    public AkunUser getReplyTo() {
        return this.replyTo;
    }

    public void postReply() {
        // Logic for posting reply can be handled in service, this method satisfies class diagram
    }

    public int getBookmark() {
        return this.bookmark;
    }

    public void setKonten(String konten) {
        this.konten = konten;
    }

    @Override
    public String typing() {
        if (user != null) {
            return user.getUsername() + " is typing a reply...";
        }
        return "Someone is typing a reply...";
    }

    // Getters and setters for id, reply list, replyTo, konten
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<Reply> getReply_list() {
        return reply;
    }

    public void setReply(List<Reply> reply) {
        this.reply = reply;
    }

    public void setReplyTo(AkunUser replyTo) {
        this.replyTo = replyTo;
    }

    public String getKonten() {
        return konten;
    }
}
