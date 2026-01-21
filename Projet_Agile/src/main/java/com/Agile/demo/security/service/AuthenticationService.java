package com.Agile.demo.security.service;

import com.Agile.demo.common.exception.BusinessException;
import com.Agile.demo.model.Role;
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

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

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

        // ✅ MODIFIÉ : Conversion des rôles en String pour la réponse
        return AuthResponse.builder()
                .token(jwt)
                .type("Bearer")
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(getRolesAsString(user.getRoles())) // Compatibilité
                .roles(user.getRoles().stream()
                        .map(Enum::name)
                        .collect(Collectors.toSet())) // Nouveau champ
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

        // ✅ MODIFIÉ : Gestion des rôles multiples
        Set<Role> roles = new HashSet<>();
        if (request.getRoles() != null && !request.getRoles().isEmpty()) {
            roles.addAll(request.getRoles());
        } else if (request.getRole() != null) {
            // Compatibilité avec l'ancien champ "role"
            roles.add(request.getRole());
        } else {
            // Rôle par défaut
            roles.add(Role.DEVELOPER);
        }

        // Créer l'utilisateur
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .roles(roles)
                .isActive(true)
                .build();

        userRepository.save(user);

        // Générer token
        String jwt = jwtTokenProvider.generateTokenFromUsername(user.getUsername());

        log.info("User {} registered successfully with roles: {}", request.getUsername(), roles);

        return AuthResponse.builder()
                .token(jwt)
                .type("Bearer")
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(getRolesAsString(user.getRoles())) // Compatibilité
                .roles(user.getRoles().stream()
                        .map(Enum::name)
                        .collect(Collectors.toSet()))
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

    /**
     * Convertit Set<Role> en String pour compatibilité
     * Si plusieurs rôles, retourne le premier (ou on peut joindre avec ",")
     */
    private String getRolesAsString(Set<Role> roles) {
        if (roles == null || roles.isEmpty()) {
            return Role.DEVELOPER.name();
        }
        // Option 1: Retourner le premier rôle
        // return roles.iterator().next().name();

        // Option 2: Retourner tous les rôles séparés par des virgules
        return roles.stream()
                .map(Enum::name)
                .collect(Collectors.joining(","));
    }
}