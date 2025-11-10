package com.csy.springbootauthbe.admin.controller;

import com.csy.springbootauthbe.admin.dto.AdminDTO;
import com.csy.springbootauthbe.admin.dto.AdminDashboardDTO;
import com.csy.springbootauthbe.admin.service.AdminService;
import com.csy.springbootauthbe.booking.dto.BookingDTO;
import com.csy.springbootauthbe.booking.dto.RecentBookingResponse;
import com.csy.springbootauthbe.student.dto.StudentDTO;
import com.csy.springbootauthbe.tutor.dto.TutorDTO;
import com.csy.springbootauthbe.user.entity.Role;
import com.csy.springbootauthbe.user.utils.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/admins")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/by-user/{userId}")
    public ResponseEntity<AdminDTO> getAdminByUserId(@PathVariable String userId) {
        Optional<AdminDTO> adminOpt = adminService.getAdminByUserId(userId);
        return adminOpt.map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/dashboard/{adminId}")
    public ResponseEntity<AdminDashboardDTO> getDashboardSummary(@PathVariable String adminId) {
        return ResponseEntity.ok(adminService.getDashboardSummary(adminId));
    }

    @GetMapping("/admins/{adminId}")
    public ResponseEntity<List<UserResponse>> getAllAdmins(@PathVariable String adminId) {
        return ResponseEntity.ok(adminService.viewAdmins(adminId));
    }

    @PutMapping("/suspendAdmin/{adminId}/{userId}")
    public ResponseEntity<UserResponse> suspendAdmin(@PathVariable String adminId, @PathVariable String userId) {
        String updatedUserId = adminService.suspendAdmin(adminId, userId);
        return ResponseEntity.ok(UserResponse.builder().id(updatedUserId).role(Role.ADMIN).build());
    }

    @PutMapping("/activateAdmin/{adminId}/{userId}")
    public ResponseEntity<UserResponse> activateAdmin(@PathVariable String adminId, @PathVariable String userId) {
        String updatedUserId = adminService.activateAdmin(adminId, userId);
        return ResponseEntity.ok(UserResponse.builder().id(updatedUserId).role(Role.ADMIN).build());
    }

    @DeleteMapping("/admin/{adminId}/{userId}")
    public ResponseEntity<UserResponse> deleteAdmin(@PathVariable String adminId, @PathVariable String userId) {
        String deletedUserId = adminService.deleteAdmin(adminId, userId);
        return ResponseEntity.ok(UserResponse.builder().id(deletedUserId).role(Role.ADMIN).build());
    }

    @GetMapping("/tutors/{adminId}")
    public ResponseEntity<List<TutorDTO>> getAllTutors(@PathVariable String adminId) {
        return ResponseEntity.ok(adminService.viewTutors(adminId));
    }

    @GetMapping("/getTutorDetails/{tutorId}")
    public ResponseEntity<Optional<TutorDTO>> getTutorDetails(@PathVariable String tutorId) {
        return ResponseEntity.ok(adminService.viewTutorDetail(tutorId));
    }

    @PutMapping("/suspendTutor/{adminId}/{userId}")
    public ResponseEntity<UserResponse> suspendTutor(@PathVariable String adminId, @PathVariable String userId) {
        String updatedUserId = adminService.suspendTutor(adminId, userId);
        return ResponseEntity.ok(UserResponse.builder().id(updatedUserId).role(Role.TUTOR).build());
    }

    @PutMapping("/activateTutor/{adminId}/{userId}")
    public ResponseEntity<UserResponse> activateTutor(@PathVariable String adminId, @PathVariable String userId) {
        String updatedUserId = adminService.activateTutor(adminId, userId);
        return ResponseEntity.ok(UserResponse.builder().id(updatedUserId).role(Role.TUTOR).build());
    }

    @PutMapping("/approveTutor/{adminId}/{userId}")
    public ResponseEntity<UserResponse> approveTutor(@PathVariable String adminId, @PathVariable String userId) {
        String updatedUserId = adminService.approveTutor(adminId, userId);
        return ResponseEntity.ok(UserResponse.builder().id(updatedUserId).role(Role.TUTOR).build());
    }

    @PutMapping("/rejectTutor/{adminId}/{userId}")
    public ResponseEntity<UserResponse> rejectTutor(@PathVariable String adminId, @PathVariable String userId,
                                                    @RequestBody Map<String, String> payload) {
        String reason = payload.get("reason");
        String updatedUserId = adminService.rejectTutor(adminId, userId, reason);
        return ResponseEntity.ok(UserResponse.builder().id(updatedUserId).role(Role.TUTOR).build());
    }

    @DeleteMapping("/tutor/{adminId}/{userId}")
    public ResponseEntity<UserResponse> deleteTutor(@PathVariable String adminId, @PathVariable String userId) {
        String deletedUserId = adminService.deleteTutor(adminId, userId);
        return ResponseEntity.ok(UserResponse.builder().id(deletedUserId).role(Role.TUTOR).build());
    }

    @GetMapping("/students/{adminId}")
    public ResponseEntity<List<UserResponse>> getAllStudents(@PathVariable String adminId) {
        return ResponseEntity.ok(adminService.viewStudents(adminId));
    }

    @GetMapping("/getStudentDetails/{studentId}")
    public ResponseEntity<Optional<StudentDTO>> getStudentDetails(@PathVariable String studentId) {
        return ResponseEntity.ok(adminService.viewStudentDetail(studentId));
    }

    @PutMapping("/suspendStudent/{adminId}/{userId}")
    public ResponseEntity<UserResponse> suspendStudent(@PathVariable String adminId, @PathVariable String userId) {
        String updatedUserId = adminService.suspendStudent(adminId, userId);
        return ResponseEntity.ok(UserResponse.builder().id(updatedUserId).role(Role.STUDENT).build());
    }

    @PutMapping("/activateStudent/{adminId}/{userId}")
    public ResponseEntity<UserResponse> activateStudent(@PathVariable String adminId, @PathVariable String userId) {
        String updatedUserId = adminService.activateStudent(adminId, userId);
        return ResponseEntity.ok(UserResponse.builder().id(updatedUserId).role(Role.STUDENT).build());
    }

    @DeleteMapping("/student/{adminId}/{userId}")
    public ResponseEntity<UserResponse> deleteStudent(@PathVariable String adminId, @PathVariable String userId) {
        String deletedUserId = adminService.deleteStudent(adminId, userId);
        return ResponseEntity.ok(UserResponse.builder().id(deletedUserId).role(Role.STUDENT).build());
    }
    @DeleteMapping("/booking/{adminId}/{bookingId}")
    public ResponseEntity<BookingDTO> deleteBookingById(@PathVariable String adminId, @PathVariable String bookingId) {
        return ResponseEntity.ok(adminService.deleteBooking(adminId, bookingId));
    }
}
