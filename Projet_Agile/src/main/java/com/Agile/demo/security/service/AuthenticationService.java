package com.Agile.demo.security.service;

import com.Agile.demo.common.exception.BusinessException;
import com.Agile.demo.model.User;
import com.Agile.demo.execution.repositories.UserRepository;
import com.Agile.demo.security.dto.AuthResponse;
import com.Agile.demo.security.dto.LoginRequest;
import com.Agile.demo.security.dto.RegisterRequest;
import com.Agile.demo.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service d'authentification et d'inscription
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * Authentifie un utilisateur et génère un token JWT
     */
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        log.info("Attempting login for user: {}", request.getUsername());

        // Authentifier
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Générer token
        String jwt = jwtTokenProvider.generateToken(authentication);

        // Récupérer l'utilisateur
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BusinessException("User not found"));

        log.info("User {} logged in successfully", request.getUsername());

        return AuthResponse.builder()
                .token(jwt)
                .type("Bearer")
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }

    /**
     * Inscrit un nouvel utilisateur
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Attempting registration for user: {}", request.getUsername());

        // Vérifications
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException("Username already taken");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Email already in use");
        }

        // Créer l'utilisateur
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .role(request.getRole() != null ? request.getRole() : com.Agile.demo.model.Role.DEVELOPER)
                .build();

        userRepository.save(user);

        // Générer token
        String jwt = jwtTokenProvider.generateTokenFromUsername(user.getUsername());

        log.info("User {} registered successfully", request.getUsername());

        return AuthResponse.builder()
                .token(jwt)
                .type("Bearer")
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }

    /**
     * Récupère l'utilisateur actuellement authentifié
     */
    @Transactional(readOnly = true)
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("Current user not found"));
    }
}