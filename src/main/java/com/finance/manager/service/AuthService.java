package com.finance.manager.service;

import com.finance.manager.dto.request.LoginRequest;
import com.finance.manager.dto.request.RegisterRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Map;

/**
 * Service interface for user authentication operations.
 */
public interface AuthService {

    /**
     * Registers a new user.
     * @return map with message and userId
     */
    Map<String, Object> register(RegisterRequest request);

    /**
     * Authenticates a user and creates a session.
     * @return map with message
     */
    Map<String, String> login(LoginRequest request, HttpServletRequest httpRequest, HttpServletResponse httpResponse);

    /**
     * Invalidates the current user's session.
     * @return map with message
     */
    Map<String, String> logout(HttpServletRequest request);
}
