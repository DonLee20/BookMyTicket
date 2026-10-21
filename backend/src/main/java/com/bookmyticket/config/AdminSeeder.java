package com.bookmyticket.config;

import com.bookmyticket.identity.Role;
import com.bookmyticket.identity.RoleRepository;
import com.bookmyticket.identity.User;
import com.bookmyticket.identity.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Creates or updates the administrator account from environment variables.
 * When no password is configured, no administrator is created and a warning is
 * logged; credentials are never hardcoded or printed.
 */
@Component
public class AdminSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminSeeder.class);

    private final AppProperties properties;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminSeeder(AppProperties properties, UserRepository userRepository, RoleRepository roleRepository,
                       PasswordEncoder passwordEncoder) {
        this.properties = properties;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        String email = properties.getAdmin().getEmail();
        String password = properties.getAdmin().getPassword();
        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            log.info("No ADMIN_EMAIL/ADMIN_PASSWORD configured; skipping administrator seeding. "
                    + "Set both in .env and restart to create the admin account.");
            return;
        }
        Role adminRole = roleRepository.findByName(Role.ADMIN)
                .orElseGet(() -> roleRepository.save(new Role(Role.ADMIN)));
        User user = userRepository.findByEmailIgnoreCase(email.trim()).orElseGet(User::new);
        boolean isNew = user.getId() == null;
        user.setName(properties.getAdmin().getName() == null || properties.getAdmin().getName().isBlank()
                ? "Platform Administrator" : properties.getAdmin().getName().trim());
        user.setEmail(email.trim().toLowerCase());
        user.setEnabled(true);
        if (isNew || !passwordEncoder.matches(password, user.getPasswordHash())) {
            user.setPasswordHash(passwordEncoder.encode(password));
        }
        user.getRoles().add(adminRole);
        userRepository.save(user);
        log.info("Administrator account {} {}.", isNew ? "created for" : "updated for", user.getEmail());
    }
}
