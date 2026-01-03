package com.file.manager.service;

import com.file.manager.config.WebclientConfig;
import com.file.manager.dto.auth.GoogleTokenResponse;
import com.file.manager.dto.auth.GoogleUserInfoResponse;
import com.file.manager.exception.AuthExcpetion;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class OAuthService {

    @Value("${oauth.google.client-id}")
    private String clientId;
    @Value("${oauth.google.client-secret}")
    private String clientSecret;
    @Value("${oauth.google.redirect-uri}")
    private String redirectUri;


    @Autowired
    private WebclientConfig webClientConfig;

    public Mono<GoogleTokenResponse> exchangeCodeForTokens(String code) {
        return webClientConfig.webClientBuilder()
                .build()
                .post()
                .uri("https://oauth2.googleapis.com/token")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .body(BodyInserters.fromFormData("code", code)
                        .with("redirect_uri", redirectUri)
                        .with("client_id", clientId)
                        .with("client_secret", clientSecret)
                        .with("scope", "")
                        .with("grant_type", "authorization_code"))
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        response -> response.bodyToMono(String.class)
                                .flatMap(errorBody -> Mono.error(new AuthExcpetion(
                                        "Error exchanging code for tokens: " + errorBody))))
                .bodyToMono(GoogleTokenResponse.class);
    }

    public Mono<GoogleUserInfoResponse> getUserInfo(String accessToken) {
        return webClientConfig.webClientBuilder()
                .build()
                .get()
                .uri("https://www.googleapis.com/oauth2/v2/userinfo")
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        response -> response.bodyToMono(String.class)
                                .flatMap(errorBody -> Mono.error(new AuthExcpetion(
                                        "Error fetching user info: " + errorBody))))
                .bodyToMono(GoogleUserInfoResponse.class);

    }


}
