package ar.com.ropalista.auth.application;

import ar.com.ropalista.auth.domain.Role;
import ar.com.ropalista.auth.domain.UserAccount;
import ar.com.ropalista.auth.persistence.UserAccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Component
@Profile("dev")
public class DevAdminInitializer implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(DevAdminInitializer.class);
    private final UserAccountRepository users;
    private final PasswordEncoder encoder;
    private final String username;
    private final String password;

    public DevAdminInitializer(UserAccountRepository users, PasswordEncoder encoder,
                               @Value("${app.dev.admin.username}") String username,
                               @Value("${app.dev.admin.password}") String password) {
        this.users = users;
        this.encoder = encoder;
        this.username = username;
        this.password = password;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!users.existsByUsernameIgnoreCase(username)) {
            users.save(new UserAccount(username, encoder.encode(password), Set.of(Role.ADMIN)));
            log.warn("development_admin_created username={} change_default_password=true", username);
        }
    }
}
