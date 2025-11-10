package com.csy.springbootauthbe.config;

import com.csy.springbootauthbe.user.entity.AccountStatus;
import com.csy.springbootauthbe.user.entity.User;
import com.csy.springbootauthbe.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ApplicationConfigTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthenticationConfiguration authenticationConfiguration;

    @InjectMocks
    private ApplicationConfig config;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testUserDetailsService_FindsUser() {
        User mockUser = new User();
        mockUser.setEmail("a@b.com");
        when(userRepository.findByEmailAndStatusNot("a@b.com", AccountStatus.DELETED))
            .thenReturn(Optional.of(mockUser));

        var service = config.userDetailsService();
        var user = service.loadUserByUsername("a@b.com");

        assertNotNull(user);
        verify(userRepository, times(1))
            .findByEmailAndStatusNot("a@b.com", AccountStatus.DELETED);
    }

    @Test
    void testUserDetailsService_UserNotFound() {
        when(userRepository.findByEmailAndStatusNot("x@y.com", AccountStatus.DELETED))
            .thenReturn(Optional.empty());

        var service = config.userDetailsService();

        assertThrows(UsernameNotFoundException.class,
            () -> service.loadUserByUsername("x@y.com"));
    }

    @Test
    void testAuthProvider_ReturnsDaoAuthProvider() {
        var provider = config.authProvider();
        assertTrue(provider instanceof DaoAuthenticationProvider);
    }

    @Test
    void testAuthManager_ReturnsFromConfig() throws Exception {
        AuthenticationManager mockManager = mock(AuthenticationManager.class);
        when(authenticationConfiguration.getAuthenticationManager()).thenReturn(mockManager);

        var manager = config.authManager(authenticationConfiguration);
        assertNotNull(manager);
        assertEquals(mockManager, manager);
    }

    @Test
    void testPasswordEncoder_ReturnsBCrypt() {
        PasswordEncoder encoder = config.passwordEncoder();
        assertNotNull(encoder);
        assertTrue(encoder.matches("pw", encoder.encode("pw")));
    }
}
