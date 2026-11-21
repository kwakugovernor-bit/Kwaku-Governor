package com.africanheritagehub.service;

import com.africanheritagehub.domain.*;
import com.africanheritagehub.repo.UserRepository;
import com.africanheritagehub.repo.WorkerProfileRepository;
import com.africanheritagehub.web.form.RegistrationForm;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService implements UserDetailsService {
    private final UserRepository users;
    private final WorkerProfileRepository profiles;
    private final PasswordEncoder encoder;

    public UserService(UserRepository users, WorkerProfileRepository profiles, PasswordEncoder encoder) {
        this.users = users;
        this.profiles = profiles;
        this.encoder = encoder;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User u = users.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UsernameNotFoundException(email));
        return new org.springframework.security.core.userdetails.User(
                u.getEmail(), u.getPasswordHash(), u.isEnabled(), true, true, true,
                List.of(new SimpleGrantedAuthority("ROLE_" + u.getRole().name())));
    }

    public User byEmail(String email) {
        return users.findByEmailIgnoreCase(email).orElseThrow();
    }

    @Transactional
    public User register(RegistrationForm f) {
        if (f.getRole() == Role.ADMIN) throw new BusinessException("error.role");
        if (users.existsByEmailIgnoreCase(f.getEmail())) throw new BusinessException("error.email.taken");
        if (f.getRole() == Role.WORKER && f.getCategory() == null) throw new BusinessException("error.category.required");

        User u = new User();
        u.setEmail(f.getEmail().trim().toLowerCase());
        u.setFullName(f.getFullName().trim());
        u.setPhone(f.getPhone().trim());
        u.setRegion(f.getRegion());
        u.setRole(f.getRole());
        u.setPasswordHash(encoder.encode(f.getPassword()));
        users.save(u);

        if (u.getRole() == Role.WORKER) {
            WorkerProfile p = new WorkerProfile();
            p.setUser(u);
            p.setCategory(f.getCategory());
            p.setRegion(f.getRegion());
            profiles.save(p);
        }
        return u;
    }

    public User createAdmin(String email, String rawPassword) {
        User u = new User();
        u.setEmail(email.toLowerCase());
        u.setFullName("Hub Administrator");
        u.setRole(Role.ADMIN);
        u.setPasswordHash(encoder.encode(rawPassword));
        return users.save(u);
    }
}
