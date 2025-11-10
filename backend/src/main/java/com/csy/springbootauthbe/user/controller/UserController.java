package com.csy.springbootauthbe.user.controller;

import com.csy.springbootauthbe.user.utils.RegisterRequest;
import com.csy.springbootauthbe.user.utils.UserResponse;
import com.csy.springbootauthbe.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService service;

    @GetMapping("/currentAdmin")
    public ResponseEntity<UserResponse> getCurrentAdmin() {
        return ResponseEntity.ok(service.getCurrentAdmin());
    }

    @GetMapping("/currentUser")
    public ResponseEntity<UserResponse> getCurrentUser() {
        return ResponseEntity.ok(service.getCurrentUser());
    }

    @GetMapping("/currentStudent")
    public ResponseEntity<UserResponse> getCurrentStudent() {
        return ResponseEntity.ok(service.getCurrentStudent());
    }

    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Hello from protected endpoint");
    }

    @GetMapping("/admins")
    public ResponseEntity<List<UserResponse>> getAllAdmins() { return ResponseEntity.ok(service.getAllAdmins());}

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable Integer id,
            @RequestBody RegisterRequest request
    ) {
        UserResponse updatedUser = service.updateUser(String.valueOf(id), request);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable String id) {
        service.deleteUser(id);
        return ResponseEntity.ok("User deleted successfully.");
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Integer id) {
        return ResponseEntity.ok(service.getUserById(String.valueOf(id)));
    }

}
