package com.finance.manager.controller;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.finance.manager.exception.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void handleNotFound_Returns404() {
        ResponseEntity<Map<String, String>> response =
                handler.handleNotFound(new ResourceNotFoundException("Not found"));
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Not found", response.getBody().get("message"));
    }

    @Test
    void handleConflict_Returns409() {
        ResponseEntity<Map<String, String>> response =
                handler.handleConflict(new ConflictException("Conflict"));
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    void handleForbidden_Returns403() {
        ResponseEntity<Map<String, String>> response =
                handler.handleForbidden(new ForbiddenException("Forbidden"));
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void handleValidation_Returns400() {
        ResponseEntity<Map<String, String>> response =
                handler.handleValidation(new ValidationException("Bad input"));
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void handleMethodArgumentNotValid_Returns400WithErrors() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("obj", "username", "must not be blank");

        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        ResponseEntity<Map<String, Object>> response = handler.handleMethodArgumentNotValid(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody().get("errors"));
        Map<?, ?> errors = (Map<?, ?>) response.getBody().get("errors");
        assertEquals("must not be blank", errors.get("username"));
    }

    @Test
    void handleMethodArgumentNotValid_FallsBackWhenDefaultMessageMissing() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("obj", "amount", null, false, null, null, null);

        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        ResponseEntity<Map<String, Object>> response = handler.handleMethodArgumentNotValid(ex);

        Map<?, ?> errors = (Map<?, ?>) response.getBody().get("errors");
        assertEquals("Invalid value", errors.get("amount"));
    }

    @Test
    void handleHttpMessageNotReadable_WithInvalidFormatCause() {
        JsonParser parser = mock(JsonParser.class);
        InvalidFormatException ife = new InvalidFormatException(parser, "bad format", "abc", String.class);
        ife.prependPath(new Object(), "amount");

        HttpMessageNotReadableException ex =
                new HttpMessageNotReadableException("msg", ife, mock(HttpInputMessage.class));

        ResponseEntity<Map<String, Object>> response = handler.handleHttpMessageNotReadable(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Map<?, ?> errors = (Map<?, ?>) response.getBody().get("errors");
        assertTrue(errors.containsKey("amount"));
    }

    @Test
    void handleHttpMessageNotReadable_WithoutInvalidFormatCause() {
        HttpMessageNotReadableException ex =
                new HttpMessageNotReadableException("malformed body", mock(HttpInputMessage.class));

        ResponseEntity<Map<String, Object>> response = handler.handleHttpMessageNotReadable(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Map<?, ?> errors = (Map<?, ?>) response.getBody().get("errors");
        assertEquals("Invalid request body", errors.get("request"));
    }

    @Test
    void handleTypeMismatch_Returns400() {
        MethodArgumentTypeMismatchException ex = mock(MethodArgumentTypeMismatchException.class);
        when(ex.getName()).thenReturn("categoryId");

        ResponseEntity<Map<String, String>> response = handler.handleTypeMismatch(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().get("message").contains("categoryId"));
    }

    @Test
    void handleAuthException_Returns401() {
        ResponseEntity<Map<String, String>> response =
                handler.handleAuthException(new BadCredentialsException("bad creds"));

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Invalid username or password", response.getBody().get("message"));
    }

    @Test
    void handleIllegalArgument_Returns400() {
        ResponseEntity<Map<String, String>> response =
                handler.handleIllegalArgument(new IllegalArgumentException("bad arg"));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("bad arg", response.getBody().get("message"));
    }

    @Test
    void handleMethodNotSupported_Returns404() {
        org.springframework.web.HttpRequestMethodNotSupportedException ex =
                new org.springframework.web.HttpRequestMethodNotSupportedException("GET");

        ResponseEntity<Map<String, String>> response = handler.handleMethodNotSupported(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void handleGeneric_Returns500() {
        ResponseEntity<Map<String, String>> response =
                handler.handleGeneric(new RuntimeException("Unexpected"));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }
}
