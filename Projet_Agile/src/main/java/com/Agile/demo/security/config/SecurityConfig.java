package com.Agile.demo.security.config;

import com.Agile.demo.security.jwt.JwtAuthenticationFilter;
import com.Agile.demo.security.service.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configuration de sécurité Spring Security avec JWT
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final UserDetailsServiceImpl userDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        // Endpoints publics
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/h2-console/**").permitAll()

                        // Endpoints par rôle
                        .requestMatchers(HttpMethod.POST, "/api/v1/projects/**").hasAnyRole("PRODUCT_OWNER")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/projects/**").hasAnyRole("PRODUCT_OWNER", "SCRUM_MASTER")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/projects/**").hasRole("PRODUCT_OWNER")

                        .requestMatchers(HttpMethod.POST, "/api/v1/epics/**").hasRole("PRODUCT_OWNER")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/epics/**").hasRole("PRODUCT_OWNER")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/epics/**").hasRole("PRODUCT_OWNER")

                        .requestMatchers(HttpMethod.POST, "/api/v1/user-stories/**").hasAnyRole("PRODUCT_OWNER", "SCRUM_MASTER")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/user-stories/**").hasAnyRole("PRODUCT_OWNER", "SCRUM_MASTER", "DEVELOPER")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/user-stories/**").hasRole("PRODUCT_OWNER")

                        .requestMatchers(HttpMethod.POST, "/api/v1/product-backlogs/*/prioritize").hasRole("PRODUCT_OWNER")

                        // Tous les autres endpoints nécessitent authentification
                        .anyRequest().authenticated()
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        // Pour H2 Console
        http.headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));

        return http.build();
    }
}