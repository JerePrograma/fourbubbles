package ar.com.ropalista.auth.api;

import ar.com.ropalista.auth.application.AuthService;
import ar.com.ropalista.common.api.ApiResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.Set;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;
    private final String cookieName;
    private final boolean secureCookie;

    public AuthController(AuthService authService,
                          @Value("${app.security.refresh-cookie-name}") String cookieName,
                          @Value("${app.security.secure-cookie}") boolean secureCookie) {
        this.authService = authService;
        this.cookieName = cookieName;
        this.secureCookie = secureCookie;
    }

    @PostMapping("/login")
    ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {
        var result = authService.login(request.username(), request.password());
        writeRefreshCookie(response, result.refreshToken(), authService.refreshTtlSeconds());
        return ApiResponse.ok(toResponse(result));
    }

    @PostMapping("/refresh")
    ApiResponse<AuthResponse> refresh(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = readCookie(request);
        var result = authService.refresh(refreshToken == null ? "" : refreshToken);
        writeRefreshCookie(response, result.refreshToken(), authService.refreshTtlSeconds());
        return ApiResponse.ok(toResponse(result));
    }

    @PostMapping("/logout")
    ApiResponse<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        authService.logout(readCookie(request));
        writeRefreshCookie(response, "", 0);
        return ApiResponse.ok(null);
    }

    private AuthResponse toResponse(AuthService.AuthResult result) {
        return new AuthResponse(result.accessToken(), "Bearer", result.expiresInSeconds(),
                result.user().getUsername(), result.user().getRoles().stream().map(Enum::name).collect(java.util.stream.Collectors.toSet()));
    }

    private void writeRefreshCookie(HttpServletResponse response, String value, long maxAge) {
        ResponseCookie cookie = ResponseCookie.from(cookieName, value)
                .httpOnly(true)
                .secure(secureCookie)
                .sameSite("Strict")
                .path("/api/auth")
                .maxAge(Duration.ofSeconds(maxAge))
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private String readCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;
        for (Cookie cookie : cookies) {
            if (cookieName.equals(cookie.getName())) return cookie.getValue();
        }
        return null;
    }

    public record LoginRequest(@NotBlank String username, @NotBlank String password) {}
    public record AuthResponse(String accessToken, String tokenType, long expiresInSeconds,
                               String username, Set<String> roles) {}
}
