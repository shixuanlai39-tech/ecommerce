package com.example.ecommerce.service;

import com.example.ecommerce.model.User;
import com.example.ecommerce.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    /**
     * 註冊新帳號 → 密碼加密後儲存
     */
    public User register(User user) {
        if (user == null || user.getPassword() == null) {
            return null;
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    /**
     * 登入邏輯：
     * 1. 新帳號：BCrypt 比對
     * 2. 舊帳號：明文比對 → 成功後自動升級為 BCrypt
     */
    public User login(String username, String password) {
        try {
            if (username == null || password == null) {
                return null;
            }

            User user = userRepository.findByUsername(username);
            if (user == null) {
                return null;
            }

            String storedPassword = user.getPassword();
            if (storedPassword == null) {
                return null;
            }

            // 情況 1：密碼已是 BCrypt 格式（新帳號）
            if (storedPassword.startsWith("$2a$") || storedPassword.startsWith("$2b$")) {
                if (passwordEncoder != null && passwordEncoder.matches(password, storedPassword)) {
                    return user;
                }
            }
            // 情況 2：密碼是明文（舊帳號）
            else if (storedPassword.equals(password)) {
                // 自動升級：加密明文密碼並儲存
                if (passwordEncoder != null) {
                    user.setPassword(passwordEncoder.encode(password));
                    userRepository.save(user);
                }
                return user;
            }

            return null; // 密碼錯誤

        } catch (Exception e) {
            // 防止任何異常導致 500
            System.err.println("UserService.login error: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 忘記密碼：重設密碼（加密）
     */
    public User resetPassword(String username, String newPassword) {
        try {
            if (username == null || newPassword == null) {
                return null;
            }

            User user = userRepository.findByUsername(username);
            if (user != null && passwordEncoder != null) {
                user.setPassword(passwordEncoder.encode(newPassword));
                return userRepository.save(user);
            }
            return null;

        } catch (Exception e) {
            System.err.println("UserService.resetPassword error: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}