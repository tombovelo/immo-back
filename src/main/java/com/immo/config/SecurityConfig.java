package com.immo.config;

import com.immo.filterJwt.JwtFilter;
import com.immo.service.CustomUserDetailsService;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableMethodSecurity(prePostEnabled = true) // ✅ Active les annotations comme @PreAuthorize
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final JwtUtils jwtUtils;
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean 
    public AuthenticationManager authenticationManager(HttpSecurity http, PasswordEncoder passwordEncoder) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder);
        return authenticationManagerBuilder.build();
    }
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http.csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
             // ✅ On définit la politique de session sur STATELESS
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/api/auth/**",
                    "/v3/api-docs/**",
                    "/swagger-ui/**",
                    "/swagger-resources/**",
                    "/configuration/**",
                    "/webjars/**",
                    "/api-docs/**",
                    "/manage/health"
                ).permitAll()
                 // La consultation (GET) des ressources est publique
                 .requestMatchers(
                    HttpMethod.GET, "/api/maisons/**", 
                    "/api/proprietaires/**", "/api/albums/**", 
                    "/api/types/**", "/api/photos/**" 
                 ).permitAll()

                 // === URLs pour l'utilisateur connecté (PROPRIETAIRE) ===
                 // Gère son profil, ses maisons, ses albums, etc.
                 .requestMatchers("/api/me/**").hasRole("PROPRIETAIRE")
 
                 // === URLs pour l'administrateur ===
                 // A un accès total à la section admin
                 .requestMatchers("/api/admin/**").hasRole("ADMIN")
 
                 // 💡 Authentification requise pour tout le reste de requette
                 .anyRequest().authenticated()
            );

        // Ajoute ton filtre JWT
        http.addFilterBefore(
            new JwtFilter(userDetailsService, jwtUtils),
            UsernamePasswordAuthenticationFilter.class
        );

        return http.build();
    }

    // ✅ Nouvelle configuration CORS moderne
    @Bean
    public UrlBasedCorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        // 💡 Ajoute ici les domaines autorisés
        config.setAllowedOrigins(List.of(
            "http://localhost:5173",
            "http://localhost:5174",
            "https://ton-site-front.onrender.com" // 👈 ton domaine front déployé
        ));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
    
}
