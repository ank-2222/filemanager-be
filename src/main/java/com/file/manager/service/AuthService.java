package com.file.manager.service;


import com.file.manager.dto.auth.LoginResponse;
import com.file.manager.dto.auth.RegisterRequest;
import com.file.manager.dto.auth.RegisterResponse;
import com.file.manager.exception.AuthExcpetion;
import com.file.manager.models.User;
import com.file.manager.repository.UserRepository;
import com.file.manager.utils.BcryptUtil;
import com.file.manager.utils.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static java.util.UUID.randomUUID;

@Slf4j
@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository; // Assuming you have a UserRepository for user data access

    @Autowired
    private BcryptUtil bcryptUtil; // Assuming you have a utility class for password hashing

    @Autowired
    private JwtUtil jwtUtil; // Assuming you have a utility class for JWT token generation

    public LoginResponse login(String username, String password) {


        User user = userRepository.findByEmail(username);
        if(user == null) {
            throw new AuthExcpetion("USER_NOT_FOUND");
        }
        if (!bcryptUtil.verifyPassword(password, user.getPassword())) {
            throw new AuthExcpetion("INVALID_CREDENTIALS");
        }

        // Generate JWT token
       String token=  jwtUtil.generateToken(user.getId().toString());
         return LoginResponse.builder().token(token).build();

    }

    public RegisterResponse register(RegisterRequest registerRequest) {
        // This is a placeholder for the actual registration logic.
        // In a real application, you would save the user data to a database and return a response.
        log.info("Registering user: {}", registerRequest.getEmail());
        if (userRepository.findByEmail(registerRequest.getEmail()) != null) {
           throw new AuthExcpetion("EMAIL_ALREADY_EXISTS");
        }

        User newUser = User.builder()
                .id(randomUUID())
                .email(registerRequest.getEmail())
                .password(bcryptUtil.hashPassword(registerRequest.getPassword()))
                .fullName(registerRequest.getFirstName() + " " + registerRequest.getLastName())
                .firstName(registerRequest.getFirstName())
                .lastName(registerRequest.getLastName())
                .createdAt(java.time.LocalDateTime.now())
                .updatedAt(java.time.LocalDateTime.now())
                .build();
    log.info(newUser.toString());

        // Save the user to the repository
         userRepository.save(newUser);

        return RegisterResponse.builder()
                .userId(newUser.getId().toString())
                .firstName(newUser.getFirstName())
                .lastName(newUser.getLastName())
                .email(newUser.getEmail())
                .token(jwtUtil.generateToken(newUser.getId().toString()))
                .build();



    }

}
