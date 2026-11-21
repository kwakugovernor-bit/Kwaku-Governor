package com.africanheritagehub.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(a -> a
                .requestMatchers("/css/**", "/js/**", "/images/**", "/uploads/**", "/error").permitAll()
                .requestMatchers("/", "/pricing", "/login", "/register", "/offline", "/sw.js", "/manifest.webmanifest").permitAll()
                .requestMatchers(HttpMethod.POST, "/workers/*/book").hasRole("CLIENT")
                .requestMatchers(HttpMethod.GET, "/workers", "/workers/*").permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/worker/**").hasRole("WORKER")
                .requestMatchers("/client/**").hasRole("CLIENT")
                .anyRequest().authenticated())
            .formLogin(f -> f
                .loginPage("/login")
                .defaultSuccessUrl("/dashboard", true)
                .permitAll())
            .logout(l -> l.logoutSuccessUrl("/").permitAll())
            .headers(h -> h.contentSecurityPolicy(c -> c.policyDirectives(
                "default-src 'self'; img-src 'self' data:; style-src 'self' 'unsafe-inline' https://fonts.googleapis.com; "
                + "font-src https://fonts.gstatic.com; script-src 'self'")));
        return http.build();
    }
}
