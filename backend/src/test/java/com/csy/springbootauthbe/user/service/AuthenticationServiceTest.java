package com.csy.springbootauthbe.user.service;

import com.csy.springbootauthbe.admin.dto.AdminDTO;
import com.csy.springbootauthbe.admin.service.AdminService;
import com.csy.springbootauthbe.config.JWTService;
import com.csy.springbootauthbe.student.dto.StudentDTO;
import com.csy.springbootauthbe.student.service.StudentService;
import com.csy.springbootauthbe.tutor.dto.TutorDTO;
import com.csy.springbootauthbe.tutor.service.TutorService;
import com.csy.springbootauthbe.user.entity.AccountStatus;
import com.csy.springbootauthbe.user.entity.Role;
import com.csy.springbootauthbe.user.entity.User;
import com.csy.springbootauthbe.user.repository.UserRepository;
import com.csy.springbootauthbe.user.utils.AuthenticationResponse;
import com.csy.springbootauthbe.user.utils.LoginRequest;
import com.csy.springbootauthbe.user.utils.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock UserRepository repository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock JWTService jwtService;
    @Mock AuthenticationManager authenticationManager;
    @Mock StudentService studentService;
    @Mock TutorService tutorService;
    @Mock AdminService adminService;

    @InjectMocks AuthenticationService auth;

    @Test
    void login_userNotFound_throwsIllegalArgumentException() {
        LoginRequest req = new LoginRequest();
        req.setEmail("nobody@nowhere.com");
        req.setPassword("pw");

        when(authenticationManager.authenticate(any()))
                .thenReturn(new UsernamePasswordAuthenticationToken("nobody@nowhere.com", "pw"));
        when(repository.findByEmailAndStatusNot("nobody@nowhere.com", AccountStatus.DELETED)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> auth.login(req));

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(repository).findByEmailAndStatusNot("nobody@nowhere.com",  AccountStatus.DELETED);
        verifyNoInteractions(jwtService);
    }

    @Test
    void login_badCredentials_propagates() {
        LoginRequest req = new LoginRequest();
        req.setEmail("user@x.com");
        req.setPassword("bad");

        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("bad creds"));

        assertThrows(BadCredentialsException.class, () -> auth.login(req));
        verify(repository, never()).findByEmailAndStatusNot(anyString(), any());
        verifyNoInteractions(jwtService);
    }

    @Test
    void login_ok_returnsTokenAndUser() {
        LoginRequest req = new LoginRequest();
        req.setEmail("ok@x.com");
        req.setPassword("pw");

        User dbUser = new User();
        dbUser.setId("U77");
        dbUser.setFirstname("Ok");
        dbUser.setLastname("User");
        dbUser.setEmail("ok@x.com");
        dbUser.setRole(Role.USER);

        when(authenticationManager.authenticate(any()))
                .thenReturn(new UsernamePasswordAuthenticationToken("ok@x.com", "pw"));
        when(repository.findByEmailAndStatusNot("ok@x.com", AccountStatus.DELETED)).thenReturn(Optional.of(dbUser));
        when(jwtService.generateToken(dbUser)).thenReturn("jwt-login");

        AuthenticationResponse resp = auth.login(req);

        assertNotNull(resp);
        assertEquals("U77", resp.getUser().getId());
        assertEquals(Role.USER, resp.getUser().getRole());
        assertEquals("jwt-login", resp.getUser().getToken());

        verify(authenticationManager).authenticate(any());
        verify(jwtService).generateToken(dbUser);
    }

    @Test
    void register_roleUser_createsPlainUser() {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("u@x.com");
        req.setPassword("P@ss");
        req.setFirstname("U");
        req.setLastname("X");
        req.setRole("User");

        when(repository.existsByEmailAndStatusNot("u@x.com", AccountStatus.DELETED)).thenReturn(false);
        when(passwordEncoder.encode("P@ss")).thenReturn("hashed");
        when(jwtService.generateToken(any(User.class))).thenReturn("jwt");
        when(repository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId("U10");
            return u;
        });

        AuthenticationResponse resp = auth.register(req);

        assertEquals("U10", resp.getUser().getId());
        assertEquals(Role.USER, resp.getUser().getRole());
        assertEquals("jwt", resp.getUser().getToken());

        // capture saved user
        ArgumentCaptor<User> userCap = ArgumentCaptor.forClass(User.class);
        verify(repository).save(userCap.capture());
        User saved = userCap.getValue();
        assertEquals("u@x.com", saved.getEmail());
        assertEquals("U", saved.getFirstname());
        assertEquals("X", saved.getLastname());
        assertEquals(Role.USER, saved.getRole());

        verify(studentService, never()).createStudent(any(StudentDTO.class));
        verify(tutorService, never()).createTutor(any(TutorDTO.class));
    }

    @Test
    void register_duplicateEmail_throws() {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("dupe@x.com");

        when(repository.existsByEmailAndStatusNot("dupe@x.com", AccountStatus.DELETED)).thenReturn(true);

        assertThrows(RuntimeException.class, () -> auth.register(req));
        verify(repository, never()).save(any());
    }

    @Test
    void register_invalidRole_throwsIllegalArgumentException() {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("x@x.com");
        req.setRole("NotARole");

        when(repository.existsByEmailAndStatusNot("x@x.com", AccountStatus.DELETED)).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> auth.register(req));
        verify(repository, never()).save(any());
    }

    @Test
    void register_roleTutor_createsTutorAndCallsTutorService() {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("tutor@x.com");
        req.setPassword("pw");
        req.setFirstname("T");
        req.setLastname("X");
        req.setRole("Tutor");

        when(repository.existsByEmailAndStatusNot("tutor@x.com", AccountStatus.DELETED)).thenReturn(false);
        when(passwordEncoder.encode("pw")).thenReturn("hashed");
        when(jwtService.generateToken(any(User.class))).thenReturn("jwt");
        when(repository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId("T123");
            u.setRole(Role.TUTOR);
            return u;
        });

        AuthenticationResponse resp = auth.register(req);

        assertNotNull(resp);
        assertEquals(Role.TUTOR, resp.getUser().getRole());
        verify(tutorService).createTutor(any(TutorDTO.class));
    }

    @Test
    void register_roleStudent_createsStudentAndCallsStudentService() {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("student@x.com");
        req.setPassword("pw");
        req.setFirstname("S");
        req.setLastname("Y");
        req.setRole("Student");

        when(repository.existsByEmailAndStatusNot("student@x.com", AccountStatus.DELETED)).thenReturn(false);
        when(passwordEncoder.encode("pw")).thenReturn("hashed");
        when(jwtService.generateToken(any(User.class))).thenReturn("jwt");
        when(repository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId("S123");
            u.setRole(Role.STUDENT);
            return u;
        });

        AuthenticationResponse resp = auth.register(req);

        assertNotNull(resp);
        assertEquals(Role.STUDENT, resp.getUser().getRole());
        verify(studentService).createStudent(any(StudentDTO.class));
    }

    @Test
    void register_roleAdmin_createsAdminAndCallsAdminService() {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("admin@x.com");
        req.setPassword("pw");
        req.setFirstname("S");
        req.setLastname("Y");
        req.setRole("Admin");

        when(repository.existsByEmailAndStatusNot("admin@x.com", AccountStatus.DELETED)).thenReturn(false);
        when(passwordEncoder.encode("pw")).thenReturn("hashed");
        when(jwtService.generateToken(any(User.class))).thenReturn("jwt");
        when(repository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId("S123");
            u.setRole(Role.ADMIN);
            return u;
        });

        AuthenticationResponse resp = auth.register(req);

        assertNotNull(resp);
        assertEquals(Role.ADMIN, resp.getUser().getRole());
        verify(adminService).createAdmin(any(AdminDTO.class));
    }
}
