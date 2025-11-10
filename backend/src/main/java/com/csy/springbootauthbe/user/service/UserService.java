package com.csy.springbootauthbe.user.service;

import com.csy.springbootauthbe.user.entity.AccountStatus;
import com.csy.springbootauthbe.user.entity.Role;
import com.csy.springbootauthbe.user.entity.User;
import com.csy.springbootauthbe.user.repository.UserRepository;
import com.csy.springbootauthbe.user.utils.RegisterRequest;
import com.csy.springbootauthbe.user.utils.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.EnumSet;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserResponse getCurrentAdmin() {
        // Get the authenticated user's email from the JWT token
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();

        // Retrieve the user from MongoDB
        User user = userRepository.findByEmailAndStatusNot(userEmail, AccountStatus.DELETED)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // Check if the user is an admin
        if (user.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Only admins can access this endpoint.");
        }

        return createUserResponse(user);
    }

    public UserResponse getCurrentUser() {

        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userRepository.findByEmailAndStatusNot(userEmail, AccountStatus.DELETED)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        EnumSet<Role> roles = EnumSet.allOf(Role.class);

        if (!roles.contains(user.getRole())) {
            throw new AccessDeniedException("Only Users can access this endpoint.");
        }

        return createUserResponse(user);
    }

    public UserResponse getCurrentStudent() {

        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userRepository.findByEmailAndStatusNot(userEmail, AccountStatus.DELETED)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (user.getRole() != Role.STUDENT) {
            throw new AccessDeniedException("Only Students can access this endpoint.");
        }

        return createUserResponse(user);
    }

    public List<UserResponse> getAllAdmins() {
        List<User> admins = userRepository.findAllByRole(Role.ADMIN);
        return admins.stream()
                .map(this::createUserResponse)
                .toList();
    }

    /** Get User By Id */
    public UserResponse getUserById(String id) { // Changed to String for MongoDB ObjectId
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return createUserResponse(user);
    }

    /** Update User */
    public UserResponse updateUser(String userId, RegisterRequest updatedUserData) { // String ID
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        user.setEmail(updatedUserData.getEmail());

        if (updatedUserData.getPassword() != null && !updatedUserData.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(updatedUserData.getPassword()));
        }

        userRepository.save(user);
        return createUserResponse(user);
    }

    /** Delete User */
    public void deleteUser(String userId) { // String ID
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        userRepository.delete(user);
    }

    private UserResponse createUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId()) // String now
                .name(user.getFirstname() + " " + user.getLastname())
                .email(user.getEmail())
                .role(user.getRole())
                .status(user.getStatus())
                .build();
    }
}
