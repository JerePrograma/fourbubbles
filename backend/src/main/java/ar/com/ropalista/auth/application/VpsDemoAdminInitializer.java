package ar.com.ropalista.auth.application;

import ar.com.ropalista.auth.domain.Role;
import ar.com.ropalista.auth.domain.UserAccount;
import ar.com.ropalista.auth.persistence.UserAccountRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

/** Explicit bootstrap for an isolated synthetic demo; never enables the dev profile. */
@Component
@Profile("vps-demo")
public class VpsDemoAdminInitializer implements ApplicationRunner {
    private final UserAccountRepository users;
    private final PasswordEncoder encoder;
    private final String username;
    private final String password;
    private final String viewerUsername;
    private final String viewerPassword;

    public VpsDemoAdminInitializer(UserAccountRepository users, PasswordEncoder encoder,
                                   @Value("${APP_DEMO_ADMIN_USERNAME}") String username,
                                   @Value("${APP_DEMO_ADMIN_PASSWORD}") String password,
                                   @Value("${APP_DEMO_VIEWER_USERNAME}") String viewerUsername,
                                   @Value("${APP_DEMO_VIEWER_PASSWORD}") String viewerPassword) {
        if (username == null || !username.matches("[A-Za-z0-9_.@-]{3,100}")) {
            throw new IllegalArgumentException("A valid demo administrator username is required");
        }
        if (password == null || password.length() < 24 || password.length() > 72) {
            throw new IllegalArgumentException("Demo administrator password must contain 24 to 72 characters");
        }
        if (viewerUsername == null || !viewerUsername.matches("[A-Za-z0-9_.@-]{3,100}")
                || viewerUsername.equalsIgnoreCase(username)) {
            throw new IllegalArgumentException("A distinct demo viewer username is required");
        }
        if (viewerPassword == null || viewerPassword.length() < 24 || viewerPassword.length() > 72
                || viewerPassword.equals(password)) {
            throw new IllegalArgumentException("A distinct demo viewer password of 24 to 72 characters is required");
        }
        this.users = users;
        this.encoder = encoder;
        this.username = username;
        this.password = password;
        this.viewerUsername = viewerUsername;
        this.viewerPassword = viewerPassword;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!users.existsByUsernameIgnoreCase(username)) {
            users.save(new UserAccount(username, encoder.encode(password), Set.of(Role.ADMIN)));
        }
        if (!users.existsByUsernameIgnoreCase(viewerUsername)) {
            users.save(new UserAccount(viewerUsername, encoder.encode(viewerPassword), Set.of(Role.REPORT_VIEWER)));
        }
    }
}
