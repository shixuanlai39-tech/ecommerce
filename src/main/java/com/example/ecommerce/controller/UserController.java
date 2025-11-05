package com.example.ecommerce.controller;

import com.example.ecommerce.dto.UserResponse;
import com.example.ecommerce.model.User;
import com.example.ecommerce.repository.UserRepository;
import com.example.ecommerce.service.EmailService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:8080")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    // === 註冊 ===
    @PostMapping("/register")
    @Transactional
    public ResponseEntity<?> register(@RequestBody User user) {
        String username = user.getUsername() != null ? user.getUsername().trim() : "";
        String email = user.getEmail() != null ? user.getEmail().trim() : "";

        if (username.isEmpty() || email.isEmpty() || user.getPassword() == null) {
            return ResponseEntity.status(400).body("請填寫完整資料");
        }

        if (userRepository.findByUsername(username) != null) {
            return ResponseEntity.status(400).body("此帳號已被註冊");
        }
        if (userRepository.findByEmail(email).isPresent()) {
            return ResponseEntity.status(400).body("此 Email 已被使用");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        User savedUser = userRepository.save(user);

        return ResponseEntity.ok(savedUser);
    }

    // === 登入 ===
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");

        if (username == null || password == null) {
            return ResponseEntity.status(400).body("請求格式錯誤");
        }

        User user = userRepository.findByUsername(username.trim());
        if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
            return ResponseEntity.status(401).body("帳號或密碼錯誤");
        }

        UserResponse response = new UserResponse(user.getId(), user.getUsername(), user.getEmail());
        return ResponseEntity.ok(response);
    }

    // === 登出 ===
    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        return ResponseEntity.ok().build();
    }

    // === 目前使用者 ===
    @GetMapping("/current")
    public ResponseEntity<?> getCurrentUser() {
        return ResponseEntity.ok().body(null);
    }

    // === 忘記密碼 ===
    @PostMapping("/forgot-password")
    @Transactional
    public ResponseEntity<String> forgotPassword(@RequestBody Map<String, String> request,
                                                HttpServletRequest httpRequest) {
        String username = request.get("username");
        if (username == null || username.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("請輸入帳號");
        }

        User user = userRepository.findByUsername(username.trim());
        if (user == null) {
            return ResponseEntity.badRequest().body("此帳號不存在");
        }
        if (user.getEmail() == null || user.getEmail().isEmpty()) {
            return ResponseEntity.badRequest().body("此帳號未綁定信箱");
        }

        String token = UUID.randomUUID().toString();
        user.setResetToken(token);
        user.setResetTokenExpiry(LocalDateTime.now().plusHours(1));
        userRepository.save(user);

        String resetLink = "http://" + httpRequest.getServerName() + ":" + httpRequest.getServerPort()
                + "/reset-password.html?token=" + token;
        emailService.sendResetPasswordEmail(user.getEmail(), resetLink);

        return ResponseEntity.ok("重設密碼郵件已寄送至您的信箱");
    }

    // === 重設密碼 ===
    @PostMapping("/reset-password")
    @Transactional
    public ResponseEntity<String> resetPassword(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        String newPassword = request.get("newPassword");

        if (token == null || newPassword == null || newPassword.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("資料不完整");
        }

        User user = userRepository.findByResetToken(token);
        if (user == null || user.getResetTokenExpiry() == null || user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            return ResponseEntity.badRequest().body("連結無效或已過期");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        userRepository.save(user);

        return ResponseEntity.ok("密碼已成功重設，請重新登入");
    }
}