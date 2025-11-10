package com.csy.springbootauthbe.config;

import com.csy.springbootauthbe.common.wrapper.UserDetailsServiceWrapper;
import com.csy.springbootauthbe.common.wrapper.UserDetailsWrapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JWTAuthenticationFilterTest {

    @Mock private JWTService jwtService;
    @Mock private UserDetailsServiceWrapper userDetailsService;
    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private FilterChain filterChain;
    @Mock private UserDetailsWrapper userDetails;

    private JWTAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        filter = new JWTAuthenticationFilter(jwtService, userDetailsService);
        SecurityContextHolder.clearContext();
    }

    @Test
    void testNoAuthHeader_PassesThrough() throws Exception {
        when(request.getHeader("Authorization")).thenReturn(null);
        filter.doFilterInternal(request, response, filterChain);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testValidToken_SetsAuthentication() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer abc123");
        when(jwtService.extractUsername("abc123")).thenReturn("user@example.com");
        when(userDetailsService.loadUserByUsername("user@example.com")).thenReturn(userDetails);
        when(jwtService.isTokenValid("abc123", userDetails)).thenReturn(true);

        filter.doFilterInternal(request, response, filterChain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testInvalidToken_ReturnsForbidden() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer badtoken");
        when(jwtService.extractUsername("badtoken")).thenReturn("user@example.com");
        when(userDetailsService.loadUserByUsername("user@example.com")).thenReturn(userDetails);
        when(jwtService.isTokenValid("badtoken", userDetails)).thenReturn(false);

        StringWriter sw = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(sw));

        filter.doFilterInternal(request, response, filterChain);

        verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
        assertTrue(sw.toString().contains("invalid"));
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void testExceptionDuringUserLoad_ReturnsForbidden() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer x");
        when(jwtService.extractUsername("x")).thenReturn("abc@xyz.com");
        when(userDetailsService.loadUserByUsername("abc@xyz.com")).thenThrow(new RuntimeException("Load failed"));

        StringWriter sw = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(sw));

        filter.doFilterInternal(request, response, filterChain);

        verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
        assertTrue(sw.toString().contains("Load failed"));
        verify(filterChain, never()).doFilter(request, response);
    }
}
