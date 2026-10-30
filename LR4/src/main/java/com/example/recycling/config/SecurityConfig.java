package com.example.recycling.config;

import jakarta.servlet.DispatcherType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.http.MediaType;
import org.springframework.security.web.util.matcher.RequestMatcher;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(Customizer.withDefaults())
                .authorizeHttpRequests(auth -> auth
                        .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/csrf-token").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/register").permitAll()

                        // Only administrators can manage users and reference data.
                        .requestMatchers("/api/users/**").hasRole("ADMIN")
                        .requestMatchers("/api/collection-points/**").hasRole("ADMIN")
                        .requestMatchers("/api/recycling-types/**").hasRole("ADMIN")
                        .requestMatchers("/api/bonus-entries/**").hasRole("ADMIN")

                        // A regular user can register a batch and view batches.
                        .requestMatchers(HttpMethod.POST, "/api/recycling-batches/business/register").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/recycling-batches", "/api/recycling-batches/**")
                            .hasAnyRole("USER", "ADMIN")

                        // Processing and direct editing of batches are administrative operations.
                        .requestMatchers("/api/recycling-batches/**").hasRole("ADMIN")
                        .anyRequest().authenticated())
                .httpBasic(Customizer.withDefaults())

                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, exception) -> {
                            response.setStatus(401);
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            response.setCharacterEncoding("UTF-8");
                            response.getWriter().write(
                                    "{\"status\":401,\"message\":\"Требуется аутентификация\"}");
                        })
                        .accessDeniedHandler((request, response, exception) -> {
                            response.setStatus(403);
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            response.setCharacterEncoding("UTF-8");
                            response.getWriter().write(
                                    "{\"status\":403,\"message\":\"Недостаточно прав или отсутствует CSRF-токен\"}");
                        })
                );

        return http.build();
    }
}
