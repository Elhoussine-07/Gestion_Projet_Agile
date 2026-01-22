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


                        .requestMatchers(HttpMethod.POST, "/api/users").hasRole("PRODUCT_OWNER")
                        .requestMatchers(HttpMethod.POST, "/api/users/*/activate").hasRole("PRODUCT_OWNER")
                        .requestMatchers(HttpMethod.POST, "/api/users/*/deactivate").hasRole("PRODUCT_OWNER")
                        .requestMatchers(HttpMethod.POST, "/api/users/*/reset-password").hasRole("PRODUCT_OWNER")
                        .requestMatchers(HttpMethod.POST, "/api/users/*/mark-password-changed").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/users/*/send-welcome").hasRole("PRODUCT_OWNER")


                        .requestMatchers(HttpMethod.PUT, "/api/users/*/password").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/users/*/profile").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/users/*").hasRole("PRODUCT_OWNER")


                        .requestMatchers(HttpMethod.DELETE, "/api/users/*").hasRole("PRODUCT_OWNER")


                        .requestMatchers(HttpMethod.GET, "/api/users/**").authenticated()


                        .requestMatchers(HttpMethod.POST, "/api/sprints").hasAnyRole("PRODUCT_OWNER", "SCRUM_MASTER")
                        .requestMatchers(HttpMethod.POST, "/api/sprints/*/start").hasAnyRole("PRODUCT_OWNER", "SCRUM_MASTER")
                        .requestMatchers(HttpMethod.POST, "/api/sprints/*/complete").hasAnyRole("PRODUCT_OWNER", "SCRUM_MASTER")
                        .requestMatchers(HttpMethod.POST, "/api/sprints/*/user-stories/*").hasAnyRole("PRODUCT_OWNER", "SCRUM_MASTER")


                        .requestMatchers(HttpMethod.PUT, "/api/sprints/*").hasAnyRole("PRODUCT_OWNER", "SCRUM_MASTER")


                        .requestMatchers(HttpMethod.DELETE, "/api/sprints/*").hasRole("PRODUCT_OWNER")
                        .requestMatchers(HttpMethod.DELETE, "/api/sprints/*/user-stories/*").hasAnyRole("PRODUCT_OWNER", "SCRUM_MASTER")


                        .requestMatchers(HttpMethod.GET, "/api/sprints/**").authenticated()


                        .requestMatchers(HttpMethod.POST, "/api/tasks").hasAnyRole("PRODUCT_OWNER", "SCRUM_MASTER", "DEVELOPER", "TESTER")
                        .requestMatchers(HttpMethod.POST, "/api/tasks/*/assign/*").hasAnyRole("PRODUCT_OWNER", "SCRUM_MASTER", "DEVELOPER", "TESTER")
                        .requestMatchers(HttpMethod.POST, "/api/tasks/*/unassign").hasAnyRole("PRODUCT_OWNER", "SCRUM_MASTER", "DEVELOPER", "TESTER")
                        .requestMatchers(HttpMethod.POST, "/api/tasks/*/log-hours").hasAnyRole("DEVELOPER", "SCRUM_MASTER", "TESTER")
                        .requestMatchers(HttpMethod.POST, "/api/tasks/*/start").hasAnyRole("DEVELOPER", "SCRUM_MASTER", "TESTER")
                        .requestMatchers(HttpMethod.POST, "/api/tasks/*/review").hasAnyRole("DEVELOPER", "SCRUM_MASTER", "TESTER")
                        .requestMatchers(HttpMethod.POST, "/api/tasks/*/testing").hasAnyRole("DEVELOPER", "SCRUM_MASTER", "TESTER")
                        .requestMatchers(HttpMethod.POST, "/api/tasks/*/complete").hasAnyRole("DEVELOPER", "SCRUM_MASTER", "TESTER")
                        .requestMatchers(HttpMethod.POST, "/api/tasks/*/block").hasAnyRole("DEVELOPER", "SCRUM_MASTER", "PRODUCT_OWNER", "TESTER")
                        .requestMatchers(HttpMethod.POST, "/api/tasks/*/unblock").hasAnyRole("SCRUM_MASTER", "PRODUCT_OWNER")
                        .requestMatchers(HttpMethod.POST, "/api/tasks/reassign/from/*/to/*").hasAnyRole("SCRUM_MASTER", "PRODUCT_OWNER")
                        .requestMatchers(HttpMethod.POST, "/api/tasks/*/duplicate").hasAnyRole("SCRUM_MASTER", "PRODUCT_OWNER")


                        .requestMatchers(HttpMethod.PUT, "/api/tasks/*/estimated-hours").hasAnyRole("PRODUCT_OWNER", "SCRUM_MASTER", "DEVELOPER", "TESTER")
                        .requestMatchers(HttpMethod.PUT, "/api/tasks/*/status").hasAnyRole("PRODUCT_OWNER", "SCRUM_MASTER", "DEVELOPER", "TESTER")
                        .requestMatchers(HttpMethod.PUT, "/api/tasks/*/description").hasAnyRole("PRODUCT_OWNER", "SCRUM_MASTER", "DEVELOPER", "TESTER")
                        .requestMatchers(HttpMethod.PUT, "/api/tasks/*/title").hasAnyRole("PRODUCT_OWNER", "SCRUM_MASTER", "DEVELOPER", "TESTER")


                        .requestMatchers(HttpMethod.DELETE, "/api/tasks/*").hasAnyRole("PRODUCT_OWNER", "SCRUM_MASTER")


                        .requestMatchers(HttpMethod.GET, "/api/tasks/**").authenticated()

                        .anyRequest().authenticated()
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        // Pour H2 Console
        http.headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));

        return http.build();
    }
}