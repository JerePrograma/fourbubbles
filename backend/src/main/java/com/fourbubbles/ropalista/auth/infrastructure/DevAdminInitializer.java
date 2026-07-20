package com.fourbubbles.ropalista.auth.infrastructure;

import com.fourbubbles.ropalista.auth.domain.UserAccount;
import com.fourbubbles.ropalista.auth.domain.UserRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.boot.CommandLineRunner;

@Component
@Profile("dev")
public class DevAdminInitializer implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(DevAdminInitializer.class);

    private final UserAccountRepository users;
    private final PasswordEncoder passwordEncoder;
    private final String email;
    private final String password;

    public DevAdminInitializer(UserAccountRepository users, PasswordEncoder passwordEncoder,
                               @Value("${DEV_ADMIN_EMAIL:}") String email,
                               @Value("${DEV_ADMIN_PASSWORD:}") String password) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.email = email;
        this.password = password;
    }

    @Override
    public void run(String... args) {
        if (email.isBlank() || password.isBlank()) {
            log.warn("Dev admin not created: DEV_ADMIN_EMAIL and DEV_ADMIN_PASSWORD are required");
            return;
        }
        users.findByEmailIgnoreCaseAndDeletedAtIsNull(email).orElseGet(() -> {
            UserAccount user = new UserAccount();
            user.setEmail(email.toLowerCase());
            user.setDisplayName("Administrador de desarrollo");
            user.setRole(UserRole.ADMIN);
            user.setPasswordHash(passwordEncoder.encode(password));
            log.info("Creating development administrator account");
            return users.save(user);
        });
    }
}
