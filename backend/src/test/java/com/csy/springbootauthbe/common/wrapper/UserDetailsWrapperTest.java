package com.csy.springbootauthbe.common.wrapper;

import com.csy.springbootauthbe.user.entity.AccountStatus;
import com.csy.springbootauthbe.user.entity.Role;
import com.csy.springbootauthbe.user.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserDetailsWrapperTest {

    @Test
    void testBasicFieldsAndGetters() {
        User user = new User();
        user.setEmail("john@doe.com");
        user.setPassword("p@ss");
        user.setStatus(AccountStatus.ACTIVE);
        user.setRole(Role.STUDENT);

        UserDetailsWrapper wrapper = new UserDetailsWrapper(user);

        assertEquals("john@doe.com", wrapper.getUsername());
        assertEquals("p@ss", wrapper.getPassword());
        assertTrue(wrapper.isAccountNonExpired());
        assertTrue(wrapper.isCredentialsNonExpired());
        assertTrue(wrapper.isEnabled());
        assertTrue(wrapper.isAccountNonLocked());
    }

    @Test
    @SuppressWarnings("unchecked")
    void testAuthoritiesPassThrough() {
        // Mock user
        User user = mock(User.class);
        List<GrantedAuthority> mockAuthorities =
            List.of(new SimpleGrantedAuthority("ROLE_ADMIN"));

        // ✅ doReturn() is safer for wildcards — avoids generics inference problems
        doReturn(mockAuthorities).when(user).getAuthorities();

        UserDetailsWrapper wrapper = new UserDetailsWrapper(user);
        Collection<? extends GrantedAuthority> result = wrapper.getAuthorities();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("ROLE_ADMIN", result.iterator().next().getAuthority());
        verify(user, atLeastOnce()).getAuthorities();
    }

    @Test
    void testAccountLockedWhenSuspended() {
        User user = new User();
        user.setStatus(AccountStatus.SUSPENDED);

        UserDetailsWrapper wrapper = new UserDetailsWrapper(user);
        assertFalse(wrapper.isAccountNonLocked());
    }

    @Test
    void testAccountDisabledWhenNotActive() {
        User user = new User();
        user.setStatus(AccountStatus.SUSPENDED);

        UserDetailsWrapper wrapper = new UserDetailsWrapper(user);
        assertFalse(wrapper.isEnabled());
    }

    @Test
    void testAccountEnabledWhenActive() {
        User user = new User();
        user.setStatus(AccountStatus.ACTIVE);

        UserDetailsWrapper wrapper = new UserDetailsWrapper(user);
        assertTrue(wrapper.isEnabled());
    }
}
