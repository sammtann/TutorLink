package com.csy.springbootauthbe.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JWTServiceTest {

    private JWTService jwtService;
    private UserDetails mockUser;

    @BeforeEach
    void setUp() throws Exception {
        jwtService = new JWTService();

        // Inject dummy secret key (Base64-encoded, 32+ bytes)
        String rawKey = "my-super-secret-key-which-is-long-enough-1234567890";
        String base64Key = Base64.getEncoder().encodeToString(rawKey.getBytes());
        Field secret = JWTService.class.getDeclaredField("SECRET_KEY");
        secret.setAccessible(true);
        secret.set(jwtService, base64Key);

        mockUser = mock(UserDetails.class);
        when(mockUser.getUsername()).thenReturn("user@example.com");
    }

    @Test
    void testGenerateAndExtractUsername() {
        String token = jwtService.generateToken(mockUser);
        assertNotNull(token);

        String extracted = jwtService.extractUsername(token);
        assertEquals("user@example.com", extracted);
    }

    @Test
    void testGenerateToken_WithExtraClaims() {
        Map<String, Object> claims = Map.of("role", "ADMIN");
        String token = jwtService.generateToken(claims, mockUser);
        assertNotNull(token);
        assertEquals("user@example.com", jwtService.extractUsername(token));
        assertEquals("ADMIN", jwtService.extractClaim(token, c -> c.get("role")));
    }

    @Test
    void testIsTokenValid_True() {
        String token = jwtService.generateToken(mockUser);
        assertTrue(jwtService.isTokenValid(token, mockUser));
    }

    @Test
    void testIsTokenValid_FalseWhenExpired() throws Exception {
        // Access private getSignInKey() correctly and cast to Key
        Method keyMethod = JWTService.class.getDeclaredMethod("getSignInKey");
        keyMethod.setAccessible(true);
        Key key = (Key) keyMethod.invoke(jwtService);

        // Build a manually expired token
        String token = Jwts.builder()
            .setSubject("user@example.com")
            .setIssuedAt(new Date(System.currentTimeMillis() - 10000))
            .setExpiration(new Date(System.currentTimeMillis() - 5000))
            .signWith(key, SignatureAlgorithm.HS256)
            .compact();

        assertThrows(io.jsonwebtoken.ExpiredJwtException.class, () -> jwtService.isTokenValid(token, mockUser));
    }

    @Test
    void testExtractClaim_GenericFunction() {
        String token = jwtService.generateToken(mockUser);
        Date exp = jwtService.extractClaim(token, Claims::getExpiration);
        assertNotNull(exp);
    }
}
