package com.chirp.backend.controller;

import com.chirp.backend.model.DirectMessage;
import com.chirp.backend.model.Message;
import com.chirp.backend.service.DirectMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dms")
public class DirectMessageController {

    private final DirectMessageService dmService;

    @Autowired
    public DirectMessageController(DirectMessageService dmService) {
        this.dmService = dmService;
    }

    @PostMapping
    public ResponseEntity<?> getOrCreateConversation(@RequestBody Map<String, String> body) {
        try {
            String username1 = body.get("username1");
            String username2 = body.get("username2");

            if (username1 == null || username2 == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "username1 and username2 are required"));
            }

            DirectMessage dm = dmService.getOrCreateConversation(username1, username2);
            return ResponseEntity.ok(dm);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{conversationId}/messages")
    public ResponseEntity<?> sendMessage(@PathVariable Long conversationId, @RequestBody Map<String, String> body) {
        try {
            String senderUsername = body.get("senderUsername");
            String text = body.get("text");

            if (senderUsername == null || text == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "senderUsername and text are required"));
            }

            Message msg = dmService.sendMessage(conversationId, senderUsername, text);
            return ResponseEntity.ok(msg);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<?> getConversations(@RequestParam("username") String username) {
        try {
            List<DirectMessage> conversations = dmService.getConversations(username);
            return ResponseEntity.ok(conversations);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{conversationId}/messages/{messageId}/pin")
    public ResponseEntity<?> pinMessage(@PathVariable Long conversationId, @PathVariable Long messageId) {
        try {
            DirectMessage dm = dmService.pinMessage(conversationId, messageId);
            return ResponseEntity.ok(dm);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{conversationId}/messages/{messageId}")
    public ResponseEntity<?> deleteMessage(@PathVariable Long conversationId, @PathVariable Long messageId) {
        try {
            DirectMessage dm = dmService.deleteMessage(conversationId, messageId);
            return ResponseEntity.ok(dm);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{conversationId}")
    public ResponseEntity<?> getConversation(@PathVariable Long conversationId) {
        try {
            DirectMessage dm = dmService.getConversation(conversationId);
            return ResponseEntity.ok(dm);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{conversationId}/sort")
    public ResponseEntity<?> sortMessages(@PathVariable Long conversationId) {
        try {
            DirectMessage dm = dmService.sortMessages(conversationId);
            return ResponseEntity.ok(dm);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
