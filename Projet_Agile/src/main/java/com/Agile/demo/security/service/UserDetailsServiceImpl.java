package com.Agile.demo.security.service;

import com.Agile.demo.model.User;
import com.Agile.demo.execution.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {

        //  CHANGEMENT CRITIQUE : Utiliser findByUsernameWithRoles au lieu de findByUsername
        User user = userRepository.findByUsernameWithRoles(username)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found: " + username));

        //  LOGS pour déboguer
        log.info("=== Loading user: {} ===", username);
        log.info("User ID: {}", user.getId());
        log.info("User roles from DB: {}", user.getRoles());
        log.info("Number of roles: {}", user.getRoles().size());

        Collection<? extends GrantedAuthority> authorities = getAuthorities(user);
        log.info("Authorities created: {}", authorities);
        log.info("========================");

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword())
                .authorities(authorities)
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(!user.isActive())
                .build();
    }

    private Collection<? extends GrantedAuthority> getAuthorities(User user) {
        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            log.warn("User {} has no roles!", user.getUsername());
            return java.util.Collections.emptySet();
        }

        return user.getRoles()
                .stream()
                .map(role -> {
                    String authority = "ROLE_" + role.name();
                    log.debug("Creating authority: {}", authority);
                    return new SimpleGrantedAuthority(authority);
                })
                .collect(Collectors.toSet());
    }
}