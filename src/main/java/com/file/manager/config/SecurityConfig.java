package com.file.manager.config;


import com.file.manager.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Disable CSRF for REST APIs
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // Stateless sessions
                .authorizeHttpRequests(authz -> authz
                        // Public endpoints (no authentication required)
                        .requestMatchers("/auth/**", "/oauth2/**", "/health/**").permitAll()

                        // All other endpoints require authentication
                        .anyRequest().authenticated()
                )
                // Add JWT filter before UsernamePasswordAuthenticationFilter
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // CORS config bean
    @Bean
    public org.springframework.web.cors.CorsConfigurationSource corsConfigurationSource() {
        var configuration = new org.springframework.web.cors.CorsConfiguration();
        configuration.setAllowCredentials(true); // allow cookies if using credentials
        configuration.setAllowedOrigins(java.util.List.of("http://localhost:5173","https://filemanager-fe.onrender.com","https://storewise.ankitkumar.space")); // Vite dev
        configuration.setAllowedMethods(java.util.List.of("GET","POST","PUT","PATCH","DELETE","OPTIONS")); // include OPTIONS
        configuration.setAllowedHeaders(java.util.List.of("Authorization","Content-Type","X-Requested-With","Accept","Origin"));
        configuration.setExposedHeaders(java.util.List.of("Authorization","Location")); // optional
        var source = new org.springframework.web.cors.UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

