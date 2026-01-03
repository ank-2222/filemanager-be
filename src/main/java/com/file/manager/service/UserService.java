package com.file.manager.service;

import com.file.manager.dto.auth.GoogleUserInfoResponse;
import com.file.manager.exception.AuthExcpetion;
import com.file.manager.models.User;
import com.file.manager.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public Mono<User> findUserByEmail(String email) {
        User user = userRepository.findByEmail(email);
        return Mono.fromCallable(() -> {
            if (user != null) {
                return user;
            } else {
                return null;
            }
        });

    }

    public Mono<User> createUser(GoogleUserInfoResponse userInfo) {
        User user = new User();
        user.setId(UUID.randomUUID()); // Assuming 'sub' is a unique identifier for the user
        user.setEmail(userInfo.getEmail());
        user.setFullName(userInfo.getName());
        user.setFirstName(userInfo.getGivenName());
        user.setLastName(userInfo.getFamilyName());
        user.setProfilePic(userInfo.getPicture());
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        return Mono.fromCallable(() -> userRepository.save(user));
    }

    public UserDetails loadUserById(String id) {
        User user = userRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new AuthExcpetion("User not found with id " + id));

        List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));

        String password = user.getPassword();
        if (password == null || password.isEmpty()) {
            // Set a dummy password for social login users
            password = "{noop}social-login"; // {noop} disables encoding to avoid errors
        }
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getId().toString())
                .password(password)
                .authorities(authorities)
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(false)
                .build();

    }


    public Optional<User> fetchuserById(String id) {
        return userRepository.findById(UUID.fromString(id));


    }
}
