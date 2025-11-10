package com.csy.springbootauthbe.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionConfigTest {

    private GlobalExceptionConfig config;

    @BeforeEach
    void setUp() {
        config = new GlobalExceptionConfig();
    }

    @Test
    void testHandleDataIntegrity() {
        ResponseEntity<?> response = config.handleDataIntegrity(
            new DataIntegrityViolationException("Duplicate email"));
        assertEquals(409, response.getStatusCodeValue());
        assertTrue(((Map<?, ?>) response.getBody()).get("message").toString().contains("Duplicate"));
    }

    @Test
    void testHandleBadCredentials() {
        ResponseEntity<?> response = config.handleBadCredentials();
        assertEquals(401, response.getStatusCodeValue());
        assertTrue(((Map<?, ?>) response.getBody()).get("message").toString().contains("Invalid"));
    }

    @Test
    void testHandleRuntime() {
        ResponseEntity<?> response = config.handleRuntime(new RuntimeException("Suspended"));
        assertEquals(403, response.getStatusCodeValue());
    }

    @Test
    void testHandleOtherExceptions() {
        ResponseEntity<?> response = config.handleOtherExceptions();
        assertEquals(500, response.getStatusCodeValue());
    }
}
