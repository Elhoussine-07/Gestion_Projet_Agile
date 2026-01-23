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

        // ✅ CHANGEMENT : Utiliser findByUsernameWithRoles
        User user = userRepository.findByUsernameWithRoles(request.getUsername())
                .orElseThrow(() -> new BusinessException("User not found"));

        log.info("User {} logged in successfully with roles: {}",
                request.getUsername(), user.getRoles());

        return AuthResponse.builder()
                .token(jwt)
                .type("Bearer")
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .roles(user.getRoles().stream()
                        .map(Enum::name)
                        .collect(Collectors.toSet()))
                .build();
    }

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

        // Gérer les rôles
        Set<Role> roles = new HashSet<>();

        if (request.getRoles() != null && !request.getRoles().isEmpty()) {
            roles.addAll(request.getRoles());
            log.info("Roles provided: {}", roles);
        } else {
            // Rôle par défaut si aucun n'est fourni
            roles.add(Role.DEVELOPER);
            log.info("No roles provided, using default role: DEVELOPER");
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .roles(roles)
                .isActive(true)
                .build();

        User savedUser = userRepository.save(user);

        // ✅ VÉRIFICATION : Recharger l'utilisateur avec les rôles
        User userWithRoles = userRepository.findByIdWithRoles(savedUser.getId())
                .orElseThrow(() -> new BusinessException("Failed to load saved user"));

        log.info("User {} registered and saved with {} role(s): {}",
                userWithRoles.getUsername(), userWithRoles.getRoles().size(), userWithRoles.getRoles());

        // Générer token
        String jwt = jwtTokenProvider.generateTokenFromUsername(userWithRoles.getUsername());

        return AuthResponse.builder()
                .token(jwt)
                .type("Bearer")
                .id(userWithRoles.getId())
                .username(userWithRoles.getUsername())
                .email(userWithRoles.getEmail())
                .roles(userWithRoles.getRoles().stream()
                        .map(Enum::name)
                        .collect(Collectors.toSet()))
                .build();
    }

    @Transactional(readOnly = true)
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        // ✅ CHANGEMENT : Utiliser findByUsernameWithRoles
        return userRepository.findByUsernameWithRoles(username)
                .orElseThrow(() -> new BusinessException("Current user not found"));
    }
}