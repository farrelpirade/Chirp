package com.chirp.backend.service;

import com.chirp.backend.model.AkunUser;
import com.chirp.backend.model.Bookmark;
import com.chirp.backend.model.News;
import com.chirp.backend.model.Reply;
import com.chirp.backend.model.Thread;
import com.chirp.backend.repository.AkunUserRepository;
import com.chirp.backend.repository.BookmarkRepository;
import com.chirp.backend.repository.NewsRepository;
import com.chirp.backend.repository.ReplyRepository;
import com.chirp.backend.repository.ThreadRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TimelineService {

    private final ThreadRepository threadRepository;
    private final NewsRepository newsRepository;
    private final AkunUserRepository userRepository;
    private final ReplyRepository replyRepository;
    private final BookmarkRepository bookmarkRepository;
    private final OpenRouterService openRouterService;

    @Autowired
    public TimelineService(ThreadRepository threadRepository,
                           NewsRepository newsRepository,
                           AkunUserRepository userRepository,
                           ReplyRepository replyRepository,
                           BookmarkRepository bookmarkRepository,
                           OpenRouterService openRouterService) {
        this.threadRepository = threadRepository;
        this.newsRepository = newsRepository;
        this.userRepository = userRepository;
        this.replyRepository = replyRepository;
        this.bookmarkRepository = bookmarkRepository;
        this.openRouterService = openRouterService;
    }

    public Thread posting(String username, String konten) {
        AkunUser user = userRepository.findById(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Thread thread = new Thread();
        thread.setUser(user);
        thread.setKonten(konten);
        thread.setTanggal(new Date());
        
        return threadRepository.save(thread);
    }

    public Thread repost(Long threadId) {
        Thread thread = threadRepository.findById(threadId)
                .orElseThrow(() -> new IllegalArgumentException("Thread not found"));

        thread.setRepost(thread.getRepost() + 1);
        return threadRepository.save(thread);
    }

    public Thread like(Long threadId) {
        Thread thread = threadRepository.findById(threadId)
                .orElseThrow(() -> new IllegalArgumentException("Thread not found"));

        thread.setLike(thread.getLike() + 1);
        return threadRepository.save(thread);
    }

    public Thread bookmark(Long threadId) {
        Thread thread = threadRepository.findById(threadId)
                .orElseThrow(() -> new IllegalArgumentException("Thread not found"));

        thread.setBookmark(thread.getBookmark() + 1);
        return threadRepository.save(thread);
    }

    @Transactional
    public Thread toggleBookmark(Long threadId, String username) {
        Thread thread = threadRepository.findById(threadId)
                .orElseThrow(() -> new IllegalArgumentException("Thread not found"));

        AkunUser user = userRepository.findById(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (bookmarkRepository.findByUserAndThread(user, thread).isPresent()) {
            bookmarkRepository.deleteByUserAndThread(user, thread);
            thread.setBookmark(Math.max(0, thread.getBookmark() - 1));
        } else {
            Bookmark bookmark = new Bookmark();
            bookmark.setUser(user);
            bookmark.setThread(thread);
            bookmarkRepository.save(bookmark);
            thread.setBookmark(thread.getBookmark() + 1);
        }
        return threadRepository.save(thread);
    }

    public List<Thread> getBookmarkedThreads(String username) {
        AkunUser user = userRepository.findById(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return bookmarkRepository.findAllByUserOrderByThread_TanggalDesc(user).stream()
                .map(Bookmark::getThread)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteThread(Long threadId, String username) {
        Thread thread = threadRepository.findById(threadId)
                .orElseThrow(() -> new IllegalArgumentException("Thread not found"));

        if (!thread.getUser().getUsername().equals(username)) {
            throw new IllegalArgumentException("You can only delete your own threads");
        }

        bookmarkRepository.deleteByThread(thread);
        threadRepository.delete(thread);
    }

    @Transactional
    public void deleteReply(Long replyId, String username) {
        Reply reply = replyRepository.findById(replyId)
                .orElseThrow(() -> new IllegalArgumentException("Reply not found"));

        if (!reply.getUser().getUsername().equals(username)) {
            throw new IllegalArgumentException("You can only delete your own replies");
        }

        // Find parent thread containing this reply
        Thread thread = threadRepository.findByReplyId(replyId).orElse(null);
        if (thread != null) {
            thread.getReplyList().remove(reply);
            threadRepository.save(thread);
        } else {
            // Check if it's a nested reply inside another reply
            Reply parentReply = replyRepository.findAll().stream()
                    .filter(r -> r.getReply_list().contains(reply))
                    .findFirst()
                    .orElse(null);
            if (parentReply != null) {
                parentReply.getReply_list().remove(reply);
                replyRepository.save(parentReply);
            }
        }

        replyRepository.delete(reply);
    }

    public Reply postReply(Long threadId, String username, String konten, String replyToUsername) {
        Thread thread = threadRepository.findById(threadId)
                .orElseThrow(() -> new IllegalArgumentException("Thread not found"));

        AkunUser user = userRepository.findById(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Reply reply = new Reply();
        reply.setUser(user);
        reply.setKonten(konten);
        reply.setLike(0);
        reply.setBookmark(0);

        if (replyToUsername != null) {
            AkunUser replyTo = userRepository.findById(replyToUsername).orElse(null);
            reply.setReplyTo(replyTo);
        }

        Reply savedReply = replyRepository.save(reply);
        thread.setReply(savedReply);
        threadRepository.save(thread);
        
        return savedReply;
    }

    public List<Thread> showThread() {
        return threadRepository.findAllByOrderByTanggalDesc();
    }

    public List<Thread> filterThreadTrending() {
        // Sort by like count descending
        return threadRepository.findAll().stream()
                .sorted((t1, t2) -> Integer.compare(t2.getLike(), t1.getLike()))
                .collect(Collectors.toList());
    }

    public List<Thread> filterForYouPageThread() {
        // FYP shows latest and highly interactive content
        return showThread();
    }

    public List<Thread> filterFollowedThread() {
        // Show feed, can be customized, default is chronological
        return showThread();
    }

    public List<Thread> searchThreads(String keyword) {
        return threadRepository.findByKontenContainingIgnoreCaseOrderByTanggalDesc(keyword);
    }

    public List<News> showNews() {
        return newsRepository.findAllByOrderByTanggalDesc();
    }

    public News generateNewsSummarization() {
        List<Thread> threads = threadRepository.findAll();
        
        // Filter threads within last 24 hours (simulated by taking most liked threads or all threads if database is small)
        // Sort by likes to find the most popular ones
        List<Thread> popularThreads = threads.stream()
                .sorted((t1, t2) -> Integer.compare(t2.getLike(), t1.getLike()))
                .limit(10)
                .collect(Collectors.toList());

        if (popularThreads.isEmpty()) {
            News news = new News();
            news.setJudul("No posts to summarize");
            news.setDeskripsi("There are no posts on the timeline to generate news.");
            news.setKonten("Post more threads on Chirp to see a generated news summary!");
            return newsRepository.save(news);
        }

        // Aggregate content
        StringBuilder textToSummarize = new StringBuilder("Here are the top posts on the platform today:\n");
        for (Thread t : popularThreads) {
            textToSummarize.append("- User @").append(t.getUser().username)
                    .append(" wrote: \"").append(t.getKonten())
                    .append("\" (Likes: ").append(t.getLike()).append(")\n");
        }

        String aiSummary = openRouterService.summarizeNews(textToSummarize.toString());

        News news = new News();
        news.setTanggal(new Date());
        news.setJudul("Today's Chirp Summary");
        news.setDeskripsi("AI summarized highlights of the most liked posts on Chirp today.");
        news.setKonten(aiSummary);

        return newsRepository.save(news);
    }
}
