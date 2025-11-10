package com.csy.springbootauthbe.admin.controller;

import com.csy.springbootauthbe.admin.dto.AdminDTO;
import com.csy.springbootauthbe.admin.dto.AdminDashboardDTO;
import com.csy.springbootauthbe.admin.service.AdminService;
import com.csy.springbootauthbe.booking.dto.BookingDTO;
import com.csy.springbootauthbe.student.dto.StudentDTO;
import com.csy.springbootauthbe.tutor.dto.TutorDTO;
import com.csy.springbootauthbe.user.entity.Role;
import com.csy.springbootauthbe.user.utils.UserResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AdminControllerTest {

    @Mock
    private AdminService adminService;

    @InjectMocks
    private AdminController adminController;

    private AdminDTO adminDTO;
    private AdminDashboardDTO dashboardDTO;
    private TutorDTO tutorDTO;
    private StudentDTO studentDTO;
    private BookingDTO bookingDTO;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        adminDTO = new AdminDTO();
        dashboardDTO = new AdminDashboardDTO();
        tutorDTO = new TutorDTO();
        studentDTO = new StudentDTO();
        bookingDTO = new BookingDTO();
    }

    // -------- Admin operations --------

    @Test
    void testGetAdminByUserId_Found() {
        when(adminService.getAdminByUserId("u1")).thenReturn(Optional.of(adminDTO));
        ResponseEntity<AdminDTO> response = adminController.getAdminByUserId("u1");
        assertEquals(200, response.getStatusCode().value());
        assertEquals(adminDTO, response.getBody());
    }

    @Test
    void testGetAdminByUserId_NotFound() {
        when(adminService.getAdminByUserId("missing")).thenReturn(Optional.empty());
        ResponseEntity<AdminDTO> response = adminController.getAdminByUserId("missing");
        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    void testGetDashboardSummary() {
        when(adminService.getDashboardSummary("a1")).thenReturn(dashboardDTO);
        ResponseEntity<AdminDashboardDTO> response = adminController.getDashboardSummary("a1");
        assertEquals(200, response.getStatusCode().value());
        assertEquals(dashboardDTO, response.getBody());
    }

    @Test
    void testGetAllAdmins() {
        List<UserResponse> users = List.of(new UserResponse());
        when(adminService.viewAdmins("a1")).thenReturn(users);
        ResponseEntity<List<UserResponse>> response = adminController.getAllAdmins("a1");
        assertEquals(200, response.getStatusCode().value());
        assertEquals(users, response.getBody());
    }

    @Test
    void testSuspendAdmin() {
        when(adminService.suspendAdmin("a1", "u1")).thenReturn("u1");
        ResponseEntity<UserResponse> response = adminController.suspendAdmin("a1", "u1");
        assertEquals(200, response.getStatusCode().value());
        assertEquals("u1", response.getBody().getId());
        assertEquals(Role.ADMIN, response.getBody().getRole());
    }

    @Test
    void testActivateAdmin() {
        when(adminService.activateAdmin("a1", "u1")).thenReturn("u1");
        ResponseEntity<UserResponse> response = adminController.activateAdmin("a1", "u1");
        assertEquals(200, response.getStatusCode().value());
        assertEquals(Role.ADMIN, response.getBody().getRole());
    }

    @Test
    void testDeleteAdmin() {
        when(adminService.deleteAdmin("a1", "u1")).thenReturn("u1");
        ResponseEntity<UserResponse> response = adminController.deleteAdmin("a1", "u1");
        assertEquals(200, response.getStatusCode().value());
        assertEquals("u1", response.getBody().getId());
    }

    // -------- Tutor operations --------

    @Test
    void testGetAllTutors() {
        List<TutorDTO> tutors = List.of(tutorDTO);
        when(adminService.viewTutors("a1")).thenReturn(tutors);
        ResponseEntity<List<TutorDTO>> response = adminController.getAllTutors("a1");
        assertEquals(200, response.getStatusCode().value());
        assertEquals(tutors, response.getBody());
    }

    @Test
    void testGetTutorDetails() {
        when(adminService.viewTutorDetail("t1")).thenReturn(Optional.of(tutorDTO));
        ResponseEntity<Optional<TutorDTO>> response = adminController.getTutorDetails("t1");
        assertTrue(response.getBody().isPresent());
        assertEquals(tutorDTO, response.getBody().get());
    }

    @Test
    void testSuspendTutor() {
        when(adminService.suspendTutor("a1", "u1")).thenReturn("u1");
        ResponseEntity<UserResponse> response = adminController.suspendTutor("a1", "u1");
        assertEquals(Role.TUTOR, response.getBody().getRole());
    }

    @Test
    void testActivateTutor() {
        when(adminService.activateTutor("a1", "u1")).thenReturn("u1");
        ResponseEntity<UserResponse> response = adminController.activateTutor("a1", "u1");
        assertEquals(Role.TUTOR, response.getBody().getRole());
    }

    @Test
    void testApproveTutor() {
        when(adminService.approveTutor("a1", "u1")).thenReturn("u1");
        ResponseEntity<UserResponse> response = adminController.approveTutor("a1", "u1");
        assertEquals("u1", response.getBody().getId());
        assertEquals(Role.TUTOR, response.getBody().getRole());
    }

    @Test
    void testRejectTutor() {
        Map<String, String> payload = Map.of("reason", "Incomplete profile");
        when(adminService.rejectTutor("a1", "u1", "Incomplete profile")).thenReturn("u1");

        ResponseEntity<UserResponse> response = adminController.rejectTutor("a1", "u1", payload);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("u1", response.getBody().getId());
        assertEquals(Role.TUTOR, response.getBody().getRole());
    }

    @Test
    void testDeleteTutor() {
        when(adminService.deleteTutor("a1", "u1")).thenReturn("u1");
        ResponseEntity<UserResponse> response = adminController.deleteTutor("a1", "u1");
        assertEquals("u1", response.getBody().getId());
        assertEquals(Role.TUTOR, response.getBody().getRole());
    }

    // -------- Student operations --------

    @Test
    void testGetAllStudents() {
        List<UserResponse> students = List.of(new UserResponse());
        when(adminService.viewStudents("a1")).thenReturn(students);
        ResponseEntity<List<UserResponse>> response = adminController.getAllStudents("a1");
        assertEquals(200, response.getStatusCode().value());
        assertEquals(students, response.getBody());
    }

    @Test
    void testGetStudentDetails() {
        when(adminService.viewStudentDetail("s1")).thenReturn(Optional.of(studentDTO));
        ResponseEntity<Optional<StudentDTO>> response = adminController.getStudentDetails("s1");
        assertTrue(response.getBody().isPresent());
        assertEquals(studentDTO, response.getBody().get());
    }

    @Test
    void testSuspendStudent() {
        when(adminService.suspendStudent("a1", "u1")).thenReturn("u1");
        ResponseEntity<UserResponse> response = adminController.suspendStudent("a1", "u1");
        assertEquals(Role.STUDENT, response.getBody().getRole());
    }

    @Test
    void testActivateStudent() {
        when(adminService.activateStudent("a1", "u1")).thenReturn("u1");
        ResponseEntity<UserResponse> response = adminController.activateStudent("a1", "u1");
        assertEquals(Role.STUDENT, response.getBody().getRole());
    }

    @Test
    void testDeleteStudent() {
        when(adminService.deleteStudent("a1", "u1")).thenReturn("u1");
        ResponseEntity<UserResponse> response = adminController.deleteStudent("a1", "u1");
        assertEquals("u1", response.getBody().getId());
        assertEquals(Role.STUDENT, response.getBody().getRole());
    }

    // -------- Booking operations --------

    @Test
    void testDeleteBookingById() {
        when(adminService.deleteBooking("a1", "b1")).thenReturn(bookingDTO);
        ResponseEntity<BookingDTO> response = adminController.deleteBookingById("a1", "b1");
        assertEquals(200, response.getStatusCode().value());
        assertEquals(bookingDTO, response.getBody());
    }
}
