package ar.com.ropalista.auth.application;

import ar.com.ropalista.auth.domain.Role;
import ar.com.ropalista.auth.domain.UserAccount;
import ar.com.ropalista.auth.persistence.UserAccountRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class VpsDemoAdminInitializerTest {
    private final UserAccountRepository users = mock(UserAccountRepository.class);
    private final PasswordEncoder encoder = mock(PasswordEncoder.class);
    private final String password = "synthetic-test-password-24-characters";
    private final String viewerPassword = "different-synthetic-viewer-test-password";

    @Test
    void hashesTheExplicitPasswordAndCreatesOnlyTheDemoAdministrator() {
        when(encoder.encode(password)).thenReturn("encoded-test-value");
        when(users.existsByUsernameIgnoreCase("demo-viewer")).thenReturn(true);
        initializer("demo-admin", password).run(null);
        var saved = ArgumentCaptor.forClass(UserAccount.class);
        verify(users).save(saved.capture());
        assertThat(saved.getValue().getUsername()).isEqualTo("demo-admin");
        assertThat(saved.getValue().getPasswordHash()).isEqualTo("encoded-test-value");
        assertThat(saved.getValue().getRoles()).containsExactly(Role.ADMIN);
    }

    @Test
    void preservesAnExistingAccountAndItsPassword() {
        when(users.existsByUsernameIgnoreCase("demo-admin")).thenReturn(true);
        when(users.existsByUsernameIgnoreCase("demo-viewer")).thenReturn(true);
        initializer("demo-admin", password).run(null);
        verify(users, never()).save(any());
        verifyNoInteractions(encoder);
    }

    @Test
    void refusesWeakOrMissingBootstrapCredentials() {
        assertThatThrownBy(() -> initializer("demo-admin", "short"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> initializer("", password))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void givesThePublicDemoAccountOnlyTheReportViewerRole() {
        when(users.existsByUsernameIgnoreCase("demo-admin")).thenReturn(true);
        when(encoder.encode(viewerPassword)).thenReturn("encoded-viewer-test-value");
        initializer("demo-admin", password).run(null);
        var saved = ArgumentCaptor.forClass(UserAccount.class);
        verify(users).save(saved.capture());
        assertThat(saved.getValue().getRoles()).containsExactly(Role.REPORT_VIEWER);
        assertThat(saved.getValue().getPasswordHash()).isEqualTo("encoded-viewer-test-value");
    }

    private VpsDemoAdminInitializer initializer(String username, String administratorPassword) {
        return new VpsDemoAdminInitializer(users, encoder, username, administratorPassword,
                "demo-viewer", viewerPassword);
    }
}
