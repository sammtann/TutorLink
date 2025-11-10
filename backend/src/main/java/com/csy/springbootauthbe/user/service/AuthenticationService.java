package com.csy.springbootauthbe.user.service;

import com.csy.springbootauthbe.admin.service.AdminService;
import com.csy.springbootauthbe.common.utils.SanitizedLogger;
import com.csy.springbootauthbe.student.service.StudentService;
import com.csy.springbootauthbe.tutor.service.TutorService;
import com.csy.springbootauthbe.user.entity.AccountStatus;
import com.csy.springbootauthbe.user.factory.AdminCreator;
import com.csy.springbootauthbe.user.factory.RoleEntityCreator;
import com.csy.springbootauthbe.user.factory.StudentCreator;
import com.csy.springbootauthbe.user.factory.TutorCreator;
import com.csy.springbootauthbe.user.utils.AuthenticationResponse;
import com.csy.springbootauthbe.user.utils.LoginRequest;
import com.csy.springbootauthbe.user.utils.RegisterRequest;
import com.csy.springbootauthbe.user.utils.UserResponse;
import com.csy.springbootauthbe.config.JWTService;
import com.csy.springbootauthbe.user.entity.Role;
import com.csy.springbootauthbe.user.entity.User;
import com.csy.springbootauthbe.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JWTService jwtService;
    private final AuthenticationManager authenticationManager;
    private final StudentService studentService;
    private final TutorService tutorService;
    private final AdminService adminService;

    private static final SanitizedLogger logger = SanitizedLogger.getLogger(AuthenticationService.class);

    public AuthenticationResponse register(RegisterRequest request) {
        logger.info("Register request received: email={}, role={}", request.getEmail(), request.getRole());

        if (repository.existsByEmailAndStatusNot(request.getEmail(), AccountStatus.DELETED)) {
            logger.warn("Registration failed: Email already exists - {}", request.getEmail());
            throw new DataIntegrityViolationException("Email already exists");
        }

        Role userRole = getUserRole(request);

        AccountStatus status = userRole == Role.TUTOR ?
            AccountStatus.UNVERIFIED : AccountStatus.ACTIVE;

        var user = User.builder()
                .firstname(request.getFirstname())
                .lastname(request.getLastname())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(userRole)
                .status(status)
                .build();

        repository.save(user);
        logger.info("User saved successfully: id={}, email={}", user.getId(), user.getEmail());

        Map<Role, RoleEntityCreator> creatorMap = Map.of(
                Role.STUDENT, new StudentCreator(studentService),
                Role.TUTOR, new TutorCreator(tutorService),
                Role.ADMIN, new AdminCreator(adminService)
        );

        creatorMap.getOrDefault(userRole, (u, r) -> {}).createEntity(user, request);
        logger.info("{} entity created for userId={}", userRole, user.getId());

        var jwtToken = jwtService.generateToken(user);
        logger.info("JWT generated for userId={}", user.getId());

        UserResponse userObj = UserResponse.builder()
                .id(user.getId())
                .name(user.getFirstname() + " " + user.getLastname())
                .email(user.getEmail())
                .role(user.getRole())
                .status(user.getStatus())
                .token(jwtToken)
                .build();

        logger.info("Registration successful for userId={}", user.getId());

        return AuthenticationResponse.builder()
                .message("User Registered successfully.")
                .user(userObj)
                .build();
    }

    public AuthenticationResponse login(LoginRequest request) {
        logger.info("Login request received: email={}", request.getEmail());
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
            logger.info("Authentication successful for email={}", request.getEmail());
        } catch (Exception e) {
            logger.error("Authentication failed for email={}. Error: {}", request.getEmail(), e.getMessage());
            throw e;
        }

        var user = repository.findByEmailAndStatusNot(request.getEmail(), AccountStatus.DELETED)
                .orElseThrow(() -> {
                    logger.error("User not found for email={}", request.getEmail());
                    return new IllegalArgumentException("User not found");
                });

        if (user.getStatus() == AccountStatus.SUSPENDED) {
            logger.warn("Login blocked: User is suspended. userId={}", user.getId());
            throw new RuntimeException("Your account has been suspended. Please contact administrator for support.");
        }

        if (user.getStatus() == AccountStatus.DELETED) {
            logger.warn("Login blocked: User is deleted. userId={}", user.getId());
            throw new RuntimeException("Your account has been deleted. Please contact administrator for support.");
        }

        var jwtToken = jwtService.generateToken(user);
        logger.info("JWT generated for login: userId={}", user.getId());

        UserResponse userObj = UserResponse.builder()
                .id(user.getId())
                .name(user.getFirstname() + " " + user.getLastname())
                .email(user.getEmail())
                .role(user.getRole())
                .token(jwtToken)
                .build();

        logger.info("Login successful for userId={}", user.getId());

        return AuthenticationResponse.builder()
                .message("User Login successfully.")
                .user(userObj)
                .build();
    }

    private static Role getUserRole(RegisterRequest request) {
        Role userRole;
        if ("Admin".equalsIgnoreCase(request.getRole())) {
            userRole = Role.ADMIN;
        } else if ("Student".equalsIgnoreCase(request.getRole())) {
            userRole = Role.STUDENT;
        } else if ("Tutor".equalsIgnoreCase(request.getRole())) {
            userRole = Role.TUTOR;
        } else if ("User".equalsIgnoreCase(request.getRole())) {
            userRole = Role.USER;
        } else {
            throw new IllegalArgumentException("Invalid role: " + request.getRole());
        }
        return userRole;
    }
}
