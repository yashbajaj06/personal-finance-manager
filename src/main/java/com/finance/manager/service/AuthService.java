package com.finance.manager.service;

import com.finance.manager.dto.request.LoginRequest;
import com.finance.manager.dto.request.RegisterRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Map;

/**
 * Handles user registration, session-based login, and logout.
 */
public interface AuthService {

    /**
     * Registers a new user account.
     *
     * @param request the new user's username (email), password, full name, and phone number
     * @return a response map containing a confirmation message and the new user's id
     */
    Map<String, Object> register(RegisterRequest request);

    /**
     * Authenticates a user and establishes a session, returning a session
     * cookie on the response for use on all subsequent authenticated requests.
     *
     * @param request      the login credentials
     * @param httpRequest  the current HTTP request, used to create the session
     * @param httpResponse the current HTTP response, used to set the session cookie
     * @return a response map containing a confirmation message
     */
    Map<String, String> login(LoginRequest request, HttpServletRequest httpRequest, HttpServletResponse httpResponse);

    /**
     * Invalidates the current session.
     *
     * @param request the current HTTP request, used to locate the session to invalidate
     * @return a response map containing a confirmation message
     */
    Map<String, String> logout(HttpServletRequest request);
}
