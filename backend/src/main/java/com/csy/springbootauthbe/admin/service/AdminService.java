package com.csy.springbootauthbe.admin.service;

import com.csy.springbootauthbe.admin.dto.AdminDTO;
import com.csy.springbootauthbe.admin.dto.AdminDashboardDTO;
import com.csy.springbootauthbe.admin.entity.Permissions;
import com.csy.springbootauthbe.booking.dto.BookingDTO;
import com.csy.springbootauthbe.student.dto.StudentDTO;
import com.csy.springbootauthbe.tutor.dto.TutorDTO;
import com.csy.springbootauthbe.user.utils.UserResponse;

import java.util.List;
import java.util.Optional;

public interface AdminService {
    // -------------------------------
    //  Student Management
    // -------------------------------
    List<UserResponse> viewStudents(String adminUserId);

    Optional<StudentDTO> viewStudentDetail(String studentId);

    String suspendStudent(String adminUserId, String studentId);

    String activateStudent(String adminUserId, String studentId);

    String deleteStudent(String adminUserId, String studentId);

    // -------------------------------
    //  Tutor Management
    // -------------------------------
    List<TutorDTO> viewTutors(String adminUserId);

    Optional<TutorDTO> viewTutorDetail(String tutorId);

    String approveTutor(String adminUserId, String tutorId);

    String rejectTutor(String adminUserId, String tutorId, String reason);

    String suspendTutor(String adminUserId, String tutorId);

    String activateTutor(String adminUserId, String tutorId);

    String deleteTutor(String adminUserId, String tutorId);

    // -------------------------------
    //  Admin Management
    // -------------------------------
    Optional<AdminDTO> getAdminByUserId(String userId);

    // -------------------------------
    //  Admin Management
    // -------------------------------
    List<UserResponse> viewAdmins(String adminUserId);

    void createAdmin(AdminDTO adminDTO);

    void editAdminRoles(String adminUserId, String targetAdminId, List<Permissions> newPermissions);

    String suspendAdmin(String adminUserId, String targetAdminId);

    String activateAdmin(String adminUserId, String targetAdminId);

    String deleteAdmin(String adminUserId, String targetAdminId);

    AdminDashboardDTO getDashboardSummary(String adminId);

    // -------------------------------
    //  Booking Management
    // -------------------------------
    BookingDTO deleteBooking(String adminUserId, String bookingId);
}
