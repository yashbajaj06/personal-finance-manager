package com.finance.manager.controller;

import com.finance.manager.dto.request.LoginRequest;
import com.finance.manager.dto.request.RegisterRequest;
import com.finance.manager.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST endpoints for user registration, login, and logout. These are the
 * only endpoints that do not require an existing authenticated session.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Registers a new user account.
     *
     * @return 201 Created with the new user's id, or 400/409 on invalid or duplicate input
     */
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@Valid @RequestBody RegisterRequest request) {
        Map<String, Object> response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Authenticates a user and starts a session, returning a session cookie
     * to be used on all subsequent requests.
     *
     * @return 200 OK on success, or 401 Unauthorized for invalid credentials
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {
        try {
            Map<String, String> response = authService.login(request, httpRequest, httpResponse);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid username or password"));
        }
    }

    /**
     * Invalidates the current session.
     *
     * @return 200 OK with a confirmation message
     */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(HttpServletRequest request) {
        Map<String, String> response = authService.logout(request);
        return ResponseEntity.ok(response);
    }
}
