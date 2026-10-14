package com.example.feedback.config;

import com.example.feedback.entity.Role;
import com.example.feedback.entity.User;
import com.example.feedback.repository.UserRepository;
import java.security.SecureRandom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Creates the initial administrator account on startup when none exists.
 *
 * <p>Credentials come from environment variables (ADMIN_USERNAME, ADMIN_EMAIL,
 * ADMIN_PASSWORD). If ADMIN_PASSWORD is not provided, a strong random password is
 * generated and printed to the console exactly once. This avoids hardcoding
 * production credentials while keeping local setup easy.</p>
 */
@Component
public class AdminInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminInitializer.class);
    private static final String PASSWORD_CHARS =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";
    private static final int GENERATED_PASSWORD_LENGTH = 16;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminUsername;
    private final String adminEmail;
    private final String adminPassword;

    public AdminInitializer(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.admin.username}") String adminUsername,
            @Value("${app.admin.email}") String adminEmail,
            @Value("${app.admin.password:}") String adminPassword
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminUsername = adminUsername;
        this.adminEmail = adminEmail;
        this.adminPassword = adminPassword;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (userRepository.existsByRole(Role.ADMIN)) {
            log.info("Administrator account already exists; skipping bootstrap.");
            return;
        }

        String password = (adminPassword == null || adminPassword.isBlank())
                ? generatePassword()
                : adminPassword;
        boolean generated = adminPassword == null || adminPassword.isBlank();

        User admin = new User(adminUsername, adminEmail.toLowerCase(), passwordEncoder.encode(password), Role.ADMIN);
        userRepository.save(admin);

        if (generated) {
            log.warn("==============================================================");
            log.warn(" Initial ADMIN account created");
            log.warn("   username: {}", adminUsername);
            log.warn("   password: {}", password);
            log.warn(" Store this password now; it will not be shown again.");
            log.warn("==============================================================");
        } else {
            log.info("Initial ADMIN account '{}' created from configured credentials.", adminUsername);
        }
    }

    private String generatePassword() {
        SecureRandom random = new SecureRandom();
        StringBuilder builder = new StringBuilder(GENERATED_PASSWORD_LENGTH);
        for (int i = 0; i < GENERATED_PASSWORD_LENGTH; i++) {
            builder.append(PASSWORD_CHARS.charAt(random.nextInt(PASSWORD_CHARS.length())));
        }
        return builder.toString();
    }
}
