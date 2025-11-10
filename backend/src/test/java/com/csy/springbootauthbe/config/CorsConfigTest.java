package com.csy.springbootauthbe.config;

import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.config.annotation.CorsRegistration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;

import static org.mockito.Mockito.*;

class CorsConfigTest {

    @Test
    void addCorsMappings_configuresExpectedMappings() {
        CorsConfig config = new CorsConfig();

        CorsRegistry registry = mock(CorsRegistry.class);
        CorsRegistration registration = mock(CorsRegistration.class);

        when(registry.addMapping("/api/**")).thenReturn(registration);
        when(registration.allowedOrigins(
                "http://18.212.82.113",
                "http://localhost:5174",
                "http://localhost:5173")).thenReturn(registration);
        when(registration.allowedMethods("GET", "POST", "PUT", "DELETE")).thenReturn(registration);
        when(registration.allowedHeaders("*")).thenReturn(registration);
        when(registration.allowCredentials(true)).thenReturn(registration);

        config.addCorsMappings(registry);

        verify(registry).addMapping("/api/**");
        verify(registration).allowedOrigins(
                "http://18.212.82.113",
                "http://localhost:5174",
                "http://localhost:5173");
        verify(registration).allowedMethods("GET", "POST", "PUT", "DELETE");
        verify(registration).allowedHeaders("*");
        verify(registration).allowCredentials(true);
        verifyNoMoreInteractions(registry, registration);
    }
}
