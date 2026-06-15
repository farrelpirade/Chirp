package com.chirp.backend.controller;

import com.chirp.backend.model.AkunUser;
import com.chirp.backend.service.AkunUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class AkunUserController {

    private final AkunUserService userService;

    @Autowired
    public AkunUserController(AkunUserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> body) {
        try {
            String username = body.get("username");
            String password = body.get("password");
            String email = body.get("email");
            String nomorTelpon = body.get("NomorTelpon"); // Map key matching class diagram capital
            String fullName = body.get("fullName");

            if (username == null || password == null || email == null || fullName == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "All fields are required"));
            }

            AkunUser registeredUser = userService.register(username, password, email, nomorTelpon, fullName);
            return ResponseEntity.ok(registeredUser);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        try {
            String username = body.get("username");
            String password = body.get("password");

            AkunUser loggedInUser = userService.login(username, password);
            return ResponseEntity.ok(loggedInUser);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        userService.logout(username);
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestBody Map<String, String> body) {
        try {
            String username = body.get("username");
            String fullName = body.get("fullName");
            String nomorTelpon = body.get("NomorTelpon");
            String email = body.get("email");
            String password = body.get("password");

            if (username == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Username is required"));
            }

            AkunUser updated = userService.updateProfile(username, fullName, nomorTelpon, email, password);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{username}")
    public ResponseEntity<?> getUser(@PathVariable String username) {
        return userService.findByUsername(username)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
