package com.gps.cs.service;

import com.gps.cs.entity.*;
import com.gps.cs.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Creates the first ADMIN when BOOTSTRAP_ADMIN_EMAIL and BOOTSTRAP_ADMIN_PASSWORD are set and no admin exists.
 * Both are optional and are read straight from the environment, so no secret appears in application.yml.
 * Remove the variables after the first start.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BootstrapAdminRunner implements ApplicationRunner {
    private final Environment env;
    private final UserRepository users;
    private final PasswordEncoder encoder;

    @Override
    public void run(ApplicationArguments args) {
        String email = env.getProperty("BOOTSTRAP_ADMIN_EMAIL");
        String password = env.getProperty("BOOTSTRAP_ADMIN_PASSWORD");
        if (email == null || email.isBlank() || password == null || password.isBlank()) return;
        if (users.existsByRole(Role.ADMIN)) return;
        User u = new User();
        u.setEmail(email.trim().toLowerCase());
        u.setPasswordHash(encoder.encode(password));
        u.setFullName("Administrator");
        u.setRole(Role.ADMIN);
        u.setStatus(UserStatus.ACTIVE);
        u.setKycStatus(KycStatus.NOT_SUBMITTED);
        users.save(u);
        log.warn("Bootstrap ADMIN account created. Remove BOOTSTRAP_ADMIN_* from the environment.");
    }
}
