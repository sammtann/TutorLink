package com.csy.springbootauthbe.user.service;

import com.csy.springbootauthbe.user.entity.AccountStatus;
import com.csy.springbootauthbe.user.entity.Role;
import com.csy.springbootauthbe.user.entity.User;
import com.csy.springbootauthbe.user.repository.UserRepository;
import com.csy.springbootauthbe.user.utils.RegisterRequest;
import com.csy.springbootauthbe.user.utils.UserResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private UserService userService;

    private User adminUser;
    private User studentUser;

    @BeforeEach
    void setUp() {
        adminUser = new User();
        adminUser.setId("1");
        adminUser.setFirstname("Admin");
        adminUser.setLastname("User");
        adminUser.setEmail("admin@example.com");
        adminUser.setRole(Role.ADMIN);
        adminUser.setStatus(AccountStatus.ACTIVE);

        studentUser = new User();
        studentUser.setId("2");
        studentUser.setFirstname("Student");
        studentUser.setLastname("User");
        studentUser.setEmail("student@example.com");
        studentUser.setRole(Role.STUDENT);
        studentUser.setStatus(AccountStatus.ACTIVE);
    }

    /* Success Case for getCurrentAdmin */
    @Test
    void getCurrentAdmin_shouldReturnAdminResponse() {
        mockAuthentication("admin@example.com");
        when(userRepository.findByEmailAndStatusNot("admin@example.com",AccountStatus.DELETED)).thenReturn(Optional.of(adminUser));

        UserResponse response = userService.getCurrentAdmin();

        assertEquals("Admin User", response.getName());
        assertEquals(Role.ADMIN, response.getRole());
    }

    /* Fail Case for getCurrentAdmin: User is not an Admin */
    @Test
    void getCurrentAdmin_shouldThrowIfNotAdmin() {
        mockAuthentication("student@example.com");
        when(userRepository.findByEmailAndStatusNot("student@example.com",AccountStatus.DELETED)).thenReturn(Optional.of(studentUser));

        assertThrows(AccessDeniedException.class, () -> userService.getCurrentAdmin());
    }

    /* Success Case for getCurrentStudent */
    @Test
    void getCurrentStudent_shouldReturnStudentResponse() {
        mockAuthentication("student@example.com");
        when(userRepository.findByEmailAndStatusNot("student@example.com",AccountStatus.DELETED)).thenReturn(Optional.of(studentUser));

        UserResponse response = userService.getCurrentStudent();

        assertEquals("Student User", response.getName());
        assertEquals(Role.STUDENT, response.getRole());
    }

    /* Fail Case for getCurrentStudent: User is not a Student */
    @Test
    void getCurrentStudent_shouldThrowIfNotStudent() {
        mockAuthentication("admin@example.com");
        when(userRepository.findByEmailAndStatusNot("admin@example.com",AccountStatus.DELETED)).thenReturn(Optional.of(adminUser));

        assertThrows(AccessDeniedException.class, () -> userService.getCurrentStudent());
    }

    /* Success Case for getAllAdmins */
    @Test
    void getAllAdmins_shouldReturnListOfAdmins() {
        when(userRepository.findAllByRole(Role.ADMIN)).thenReturn(List.of(adminUser));

        List<UserResponse> admins = userService.getAllAdmins();

        assertEquals(1, admins.size());
        assertEquals("Admin User", admins.get(0).getName());
    }

    /* Success Case for updateUser */
    @Test
    void updateUser_shouldUpdateAndReturnResponse() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("new@example.com");
        request.setPassword("newPass");

        when(userRepository.findById("1")).thenReturn(Optional.of(adminUser));
        when(passwordEncoder.encode("newPass")).thenReturn("encodedPass");

        UserResponse response = userService.updateUser("1", request);

        assertEquals("new@example.com", adminUser.getEmail());
        assertEquals("encodedPass", adminUser.getPassword());
        assertEquals("Admin User", response.getName());
        verify(userRepository).save(adminUser);
    }

    /* Success Case for deleteUser */
    @Test
    void deleteUser_shouldDeleteUser() {
        when(userRepository.findById("1")).thenReturn(Optional.of(adminUser));

        userService.deleteUser("1");

        verify(userRepository).delete(adminUser);
    }

    /** Helper method to mock SecurityContext with a given email */
    private void mockAuthentication(String email) {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(email);
        SecurityContextHolder.setContext(securityContext);
    }
}
