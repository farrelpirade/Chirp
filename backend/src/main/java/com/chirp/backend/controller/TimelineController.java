package com.chirp.backend.controller;

import com.chirp.backend.model.News;
import com.chirp.backend.model.Reply;
import com.chirp.backend.model.Thread;
import com.chirp.backend.model.Timeline;
import com.chirp.backend.service.TimelineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class TimelineController {

    private final TimelineService timelineService;

    @Autowired
    public TimelineController(TimelineService timelineService) {
        this.timelineService = timelineService;
    }

    @PostMapping("/threads")
    public ResponseEntity<?> posting(@RequestBody Map<String, String> body) {
        try {
            String username = body.get("username");
            String konten = body.get("konten");

            if (username == null || konten == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Username and konten are required"));
            }

            Thread thread = timelineService.posting(username, konten);
            return ResponseEntity.ok(thread);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/threads/{id}/repost")
    public ResponseEntity<?> repost(@PathVariable Long id) {
        try {
            Thread thread = timelineService.repost(id);
            return ResponseEntity.ok(thread);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/threads/{id}/like")
    public ResponseEntity<?> like(@PathVariable Long id) {
        try {
            Thread thread = timelineService.like(id);
            return ResponseEntity.ok(thread);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/threads/{id}/bookmark")
    public ResponseEntity<?> bookmark(@PathVariable Long id) {
        try {
            Thread thread = timelineService.bookmark(id);
            return ResponseEntity.ok(thread);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/threads/{id}/reply")
    public ResponseEntity<?> reply(@PathVariable Long id, @RequestBody Map<String, String> body) {
        try {
            String username = body.get("username");
            String konten = body.get("konten");
            String replyToUsername = body.get("replyToUsername");

            if (username == null || konten == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Username and konten are required"));
            }

            Reply reply = timelineService.postReply(id, username, konten, replyToUsername);
            return ResponseEntity.ok(reply);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/threads")
    public ResponseEntity<?> getThreads(@RequestParam(value = "filter", required = false) String filter,
                                        @RequestParam(value = "search", required = false) String search) {
        try {
            List<Thread> threads;
            if (search != null && !search.trim().isEmpty()) {
                threads = timelineService.searchThreads(search);
            } else if ("trending".equalsIgnoreCase(filter)) {
                threads = timelineService.filterThreadTrending();
            } else if ("followed".equalsIgnoreCase(filter)) {
                threads = timelineService.filterFollowedThread();
            } else if ("foryou".equalsIgnoreCase(filter)) {
                threads = timelineService.filterForYouPageThread();
            } else {
                threads = timelineService.showThread();
            }
            return ResponseEntity.ok(threads);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/timeline")
    public ResponseEntity<?> getTimeline() {
        try {
            List<Thread> threads = timelineService.showThread();
            List<News> news = timelineService.showNews();
            
            Timeline timeline = new Timeline(
                    threads.toArray(new Thread[0]),
                    news.toArray(new News[0])
            );
            return ResponseEntity.ok(timeline);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/news")
    public ResponseEntity<?> getNews() {
        try {
            List<News> news = timelineService.showNews();
            return ResponseEntity.ok(news);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/news/generate")
    public ResponseEntity<?> generateNews() {
        try {
            News news = timelineService.generateNewsSummarization();
            return ResponseEntity.ok(news);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
}
