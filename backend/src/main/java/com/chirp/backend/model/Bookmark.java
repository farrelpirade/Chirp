package com.chirp.backend.model;

import jakarta.persistence.*;

@Entity
@Table(name = "bookmarks", uniqueConstraints = @UniqueConstraint(columnNames = {"user_username", "thread_id"}))
public class Bookmark {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_username")
    private AkunUser user;

    @ManyToOne
    @JoinColumn(name = "thread_id")
    private Thread thread;

    public Bookmark() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public AkunUser getUser() {
        return user;
    }

    public void setUser(AkunUser user) {
        this.user = user;
    }

    public Thread getThread() {
        return thread;
    }

    public void setThread(Thread thread) {
        this.thread = thread;
    }
}
