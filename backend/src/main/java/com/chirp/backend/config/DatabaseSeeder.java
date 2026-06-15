package com.chirp.backend.config;

import com.chirp.backend.model.AkunUser;
import com.chirp.backend.model.News;
import com.chirp.backend.model.Reply;
import com.chirp.backend.model.Thread;
import com.chirp.backend.repository.AkunUserRepository;
import com.chirp.backend.repository.NewsRepository;
import com.chirp.backend.repository.ReplyRepository;
import com.chirp.backend.repository.ThreadRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Component
public class DatabaseSeeder implements CommandLineRunner {

    private final AkunUserRepository userRepository;
    private final ThreadRepository threadRepository;
    private final ReplyRepository replyRepository;
    private final NewsRepository newsRepository;

    @Autowired
    public DatabaseSeeder(AkunUserRepository userRepository,
                          ThreadRepository threadRepository,
                          ReplyRepository replyRepository,
                          NewsRepository newsRepository) {
        this.userRepository = userRepository;
        this.threadRepository = threadRepository;
        this.replyRepository = replyRepository;
        this.newsRepository = newsRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        // Only seed if database is empty
        if (userRepository.count() > 0) {
            return;
        }

        System.out.println("====== SEEDING DATABASE WITH INTERESTING POSTS & NEWS ======");

        // 1. Seed users
        AkunUser evo = new AkunUser("evo_abimanyu", "password123", "evo@chirp.com", "08123456789", "Evo Abimanyu");
        AkunUser farrel = new AkunUser("farrel_pirade", "password123", "farrel@chirp.com", "08123456780", "Farrel Malik Pirade");
        AkunUser faza = new AkunUser("faza_azima", "password123", "faza@chirp.com", "08123456781", "Faza Fawzan Azima");
        AkunUser ravi = new AkunUser("ravi_prakoso", "password123", "ravi@chirp.com", "08123456782", "Ravi Adi Prakoso");

        userRepository.saveAll(List.of(evo, farrel, faza, ravi));

        // 2. Seed threads
        Thread t1 = new Thread();
        t1.setUser(evo);
        t1.setKonten("Just launched Chirp, the ultimate neo-brutalist microblogging platform built with Spring Boot and Nuxt! 🚀 What do you guys think? #launch #chirp #webdev");
        t1.setLike(32);
        t1.setRepost(8);
        t1.setBookmark(5);
        t1.setTanggal(new Date(System.currentTimeMillis() - 3600000 * 2)); // 2 hours ago
        threadRepository.save(t1);

        Thread t2 = new Thread();
        t2.setUser(farrel);
        t2.setKonten("NVIDIA's new Nemotron-3 model on OpenRouter is surprisingly fast and smart. Perfect for our AI chatbot integration! #ai #nvidia #openrouter #tech");
        t2.setLike(45);
        t2.setRepost(12);
        t2.setBookmark(15);
        t2.setTanggal(new Date(System.currentTimeMillis() - 3600000 * 5)); // 5 hours ago
        threadRepository.save(t2);

        Thread t3 = new Thread();
        t3.setUser(faza);
        t3.setKonten("Remember: Clean code always looks like it was written by someone who cares. Simple is better than complex. Keep your packages decoupled! #programming #cleancode #java #spring");
        t3.setLike(52);
        t3.setRepost(18);
        t3.setBookmark(22);
        t3.setTanggal(new Date(System.currentTimeMillis() - 3600000 * 8)); // 8 hours ago
        threadRepository.save(t3);

        Thread t4 = new Thread();
        t4.setUser(ravi);
        t4.setKonten("Why did the programmer quit his job? Because he didn't get arrays! 😂 Get it? #joke #coding #jokeoftheday");
        t4.setLike(89);
        t4.setRepost(35);
        t4.setBookmark(40);
        t4.setTanggal(new Date(System.currentTimeMillis() - 3600000 * 12)); // 12 hours ago
        threadRepository.save(t4);

        Thread t5 = new Thread();
        t5.setUser(evo);
        t5.setKonten("Hashtags are working perfectly on the feed search! Try searching for #programming or #ai in the search box to see it in action. #feature #chirp");
        t5.setLike(18);
        t5.setRepost(2);
        t5.setBookmark(4);
        t5.setTanggal(new Date(System.currentTimeMillis() - 3600000 * 1)); // 1 hour ago
        threadRepository.save(t5);

        // 3. Seed replies to t1
        Reply r1 = new Reply();
        r1.setUser(ravi);
        r1.setKonten("This UI is gorgeous! The neo-brutalist style looks so clean and bold.");
        r1.setLike(5);
        r1.setReplyTo(evo);
        replyRepository.save(r1);

        Reply r2 = new Reply();
        r2.setUser(faza);
        r2.setKonten("Awesome work! Spring Boot + Nuxt is a killer stack. Super responsive.");
        r2.setLike(8);
        r2.setReplyTo(evo);
        replyRepository.save(r2);

        t1.setReply(r1);
        t1.setReply(r2);
        threadRepository.save(t1);

        // 4. Seed news summaries
        News n1 = new News();
        n1.setJudul("Tech Highlights: Chirp Microblogging Platform Launched");
        n1.setDeskripsi("Developer @evo_abimanyu releases a new retro neo-brutalist platform built on modern stack.");
        n1.setKonten("Chirp has officially launched! The microblogging site is receiving praise for its bold neo-brutalist web design (thick borders, high drop shadows, and high-contrast color scheme). The app features secure MVC architecture in Java Spring Boot on the backend and uses Nuxt on the frontend. Features like user authentication, interactive threads, bookmarks, replies, and integrated AI tools are working successfully.");
        n1.setTanggal(new Date(System.currentTimeMillis() - 3600000 * 1));
        newsRepository.save(n1);

        News n2 = new News();
        n2.setJudul("AI News: NVIDIA Nemotron-3 Model Free Tier Popularity Surges");
        n2.setDeskripsi("Developers are leveraging OpenRouter free tier for chatbot integration.");
        n2.setKonten("A wave of developers, including the makers of Chirp, are migrating chatbot integrations to NVIDIA's Nemotron-3 model available on OpenRouter's free tier. Known for its quick latency and 30B parameter size, the model offers a robust alternative for lightweight conversational assistants without requiring paid credits. Early benchmarks show high response quality for developer-focused instructions.");
        n2.setTanggal(new Date(System.currentTimeMillis() - 3600000 * 4));
        newsRepository.save(n2);

        System.out.println("====== SEEDING COMPLETED ======");
    }
}
