package com.file.manager.controller;

import com.file.manager.dto.auth.OAuthResponse;
import com.file.manager.exception.AuthExcpetion;
import com.file.manager.utils.JwtUtil;
import com.file.manager.service.OAuthService;
import com.file.manager.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/auth/oauth2")
public class OAuth2Controller {

    @Autowired
    private OAuthService oAuthService;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtService;

    @PostMapping("/google")
    public Mono<ResponseEntity<OAuthResponse>> googleLogin(@RequestBody Map<String, String> body) {
        String code = body.get("code");

        return oAuthService.exchangeCodeForTokens(code)
                .flatMap(tokens -> oAuthService.getUserInfo(tokens.getAccess_token()))
                .flatMap(userInfo ->
                        userService.findUserByEmail(userInfo.getEmail())
                                .flatMap(existingUser -> {
                                    log.debug("User with email {} already exists, updating information", userInfo.getEmail());
                                    return Mono.just(existingUser);
                                })
                                .switchIfEmpty(
                                        Mono.defer(() -> {
                                            return userService.createUser(userInfo);
                                        })
                                )
                )
                .map(user -> {
                    String jwt = jwtService.generateToken(user.getId().toString());
                    return ResponseEntity.ok(new OAuthResponse(jwt));
                })
                .doOnError(e -> log.error("Error during Google OAuth2 login: {}", e.getMessage()))
                .onErrorMap(e -> new AuthExcpetion(
                        e.getMessage(), e
                ));
    }


}
