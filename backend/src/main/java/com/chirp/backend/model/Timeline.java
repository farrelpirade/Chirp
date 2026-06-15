package com.chirp.backend.model;

public class Timeline {
    private Thread[] posts;
    private News[] news;

    public Timeline() {
        this.posts = new Thread[0];
        this.news = new News[0];
    }

    public Timeline(Thread[] posts, News[] news) {
        this.posts = posts;
        this.news = news;
    }

    public Thread[] showThread() {
        return this.posts;
    }

    public News[] showNews() {
        return this.news;
    }

    public Thread[] filterThreadTrending() {
        return this.posts;
    }

    public Thread[] filterForYouPageThread() {
        return this.posts;
    }

    public Thread[] filterFollowedThread() {
        return this.posts;
    }

    // Standard getters and setters
    public Thread[] getPosts() {
        return posts;
    }

    public void setPosts(Thread[] posts) {
        this.posts = posts;
    }

    public News[] getNews() {
        return news;
    }

    public void setNews(News[] news) {
        this.news = news;
    }
}
