package com.file.manager.controller;


import com.file.manager.exception.AuthExcpetion;
import com.file.manager.models.User;
import com.file.manager.service.UserService;
import com.file.manager.utils.JwtUtil;
import jakarta.security.auth.message.AuthException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping("/me")
    public ResponseEntity<User> userProfile(HttpServletRequest request) {
        try {
            // Extract JWT token directly from the Authorization header
            String bearerToken = request.getHeader("Authorization");
            String token = null;
            if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
                token = bearerToken.substring(7);
            }
            // Use your JwtUtil service directly to get subject (user id)
            String id = jwtUtil.getSubject(token);

            Optional<User> currentUser = userService.fetchuserById(id);
            return currentUser
                    .map(ResponseEntity::ok)
                    .orElseThrow(() -> new AuthException("USER_NOT_FOUND"));

        } catch (Exception e) {
            log.error("Error fetching user profile: {}", e.getMessage());
            throw new AuthExcpetion("Error fetching user profile", e);
        }
    }



}
