package com.csy.springbootauthbe.common.wrapper;

import com.csy.springbootauthbe.user.entity.AccountStatus;
import com.csy.springbootauthbe.user.entity.User;
import com.csy.springbootauthbe.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserDetailsServiceWrapperTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserDetailsServiceWrapper userDetailsServiceWrapper;

    private User activeUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        activeUser = new User();
        activeUser.setEmail("test@example.com");
        activeUser.setPassword("secret");
        activeUser.setStatus(AccountStatus.ACTIVE);
    }

    @Test
    void testLoadUserByUsername_Success() {
        when(userRepository.findByEmailAndStatusNot("test@example.com", AccountStatus.DELETED))
            .thenReturn(Optional.of(activeUser));

        UserDetailsWrapper wrapper = userDetailsServiceWrapper.loadUserByUsername("test@example.com");

        assertNotNull(wrapper);
        assertEquals("test@example.com", wrapper.getUsername());
        assertEquals("secret", wrapper.getPassword());
        verify(userRepository, times(1))
            .findByEmailAndStatusNot("test@example.com", AccountStatus.DELETED);
    }

    @Test
    void testLoadUserByUsername_NotFound_ThrowsException() {
        when(userRepository.findByEmailAndStatusNot("missing@example.com", AccountStatus.DELETED))
            .thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
            () -> userDetailsServiceWrapper.loadUserByUsername("missing@example.com"));

        verify(userRepository, times(1))
            .findByEmailAndStatusNot("missing@example.com", AccountStatus.DELETED);
    }
}
