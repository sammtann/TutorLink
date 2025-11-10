package com.csy.springbootauthbe.admin.service;

import com.csy.springbootauthbe.admin.dto.AdminDTO;
import com.csy.springbootauthbe.admin.dto.AdminDashboardDTO;
import com.csy.springbootauthbe.admin.entity.Admin;
import com.csy.springbootauthbe.admin.entity.Permissions;
import com.csy.springbootauthbe.admin.mapper.AdminMapper;
import com.csy.springbootauthbe.admin.repository.AdminRepository;
import com.csy.springbootauthbe.booking.dto.BookingDTO;
import com.csy.springbootauthbe.booking.service.BookingServiceImpl;
import com.csy.springbootauthbe.student.dto.StudentDTO;
import com.csy.springbootauthbe.student.entity.Student;
import com.csy.springbootauthbe.student.repository.StudentRepository;
import com.csy.springbootauthbe.tutor.dto.TutorDTO;
import com.csy.springbootauthbe.tutor.entity.Tutor;
import com.csy.springbootauthbe.tutor.repository.TutorRepository;
import com.csy.springbootauthbe.user.entity.AccountStatus;
import com.csy.springbootauthbe.user.entity.Role;
import com.csy.springbootauthbe.user.entity.User;
import com.csy.springbootauthbe.user.repository.UserRepository;
import com.csy.springbootauthbe.wallet.service.WalletService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AdminServiceImplTest {

    @Mock private AdminMapper adminMapper;
    @Mock private AdminRepository adminRepository;
    @Mock private UserRepository userRepository;
    @Mock private StudentRepository studentRepository;
    @Mock private TutorRepository tutorRepository;
    @Mock private WalletService walletService;
    @Mock private BookingServiceImpl bookingService;

    @InjectMocks private AdminServiceImpl service;

    private Admin admin;
    private User adminUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        adminUser = new User("A1", "Admin", "User", "admin@example.com", "pw",
            AccountStatus.ACTIVE, Role.ADMIN);
        admin = new Admin();
        admin.setUserId("A1");
        admin.setPermissions(List.of(Permissions.VIEW_STUDENTS, Permissions.VIEW_TUTORS, Permissions.VIEW_ADMIN));
    }

    // --------------- Helper ----------------
    private void mockAdminPermission(Permissions... perms) {
        admin.setPermissions(Arrays.asList(perms));
        when(userRepository.findById("A1")).thenReturn(Optional.of(adminUser));
        when(adminRepository.findByUserId("A1")).thenReturn(Optional.of(admin));
    }

    // --------------- Student Management ----------------
    @Test
    void testViewStudents_ReturnsResponses() {
        mockAdminPermission(Permissions.VIEW_STUDENTS);

        User s1 = new User("S1", "John", "Doe", "john@example.com", "pw",
            AccountStatus.ACTIVE, Role.STUDENT);
        Student st = new Student();
        st.setUserId("S1");
        st.setStudentNumber("SN001");

        when(userRepository.findAllByRole(Role.STUDENT)).thenReturn(List.of(s1));
        when(studentRepository.findByUserId("S1")).thenReturn(Optional.of(st));

        var result = service.viewStudents("A1");
        assertEquals(1, result.size());
        assertTrue(result.get(0).getName().contains("John"));
    }

    @Test
    void testViewStudentDetail_ReturnsDTO() {
        User s1 = new User("S1", "Jane", "Doe", "jane@example.com", "pw",
            AccountStatus.ACTIVE, Role.STUDENT);
        when(userRepository.findById("S1")).thenReturn(Optional.of(s1));
        Student st = new Student();
        st.setUserId("S1");
        st.setGradeLevel("Grade 5");
        when(studentRepository.findByUserId("S1")).thenReturn(Optional.of(st));

        Optional<StudentDTO> result = service.viewStudentDetail("S1");
        assertTrue(result.isPresent());
        assertEquals("Jane", result.get().getFirstName());
    }

    @Test
    void testSuspendStudent_SetsStatusSuspended() {
        mockAdminPermission(Permissions.SUSPEND_STUDENT);
        User s1 = new User("S1", "A", "B", "e@x.com", "pw",
            AccountStatus.ACTIVE, Role.STUDENT);
        when(userRepository.findById("S1")).thenReturn(Optional.of(s1));

        String id = service.suspendStudent("A1", "S1");
        assertEquals("S1", id);
        assertEquals(AccountStatus.SUSPENDED, s1.getStatus());
    }

    @Test
    void testDeleteStudent_SetsStatusDeleted() {
        mockAdminPermission(Permissions.DELETE_STUDENT);
        User s1 = new User("S1", "A", "B", "e@x.com", "pw",
            AccountStatus.ACTIVE, Role.STUDENT);
        when(userRepository.findById("S1")).thenReturn(Optional.of(s1));

        service.deleteStudent("A1", "S1");
        assertEquals(AccountStatus.DELETED, s1.getStatus());
    }

    // --------------- Tutor Management ----------------
    @Test
    void testViewTutors_ReturnsMappedTutors() {
        mockAdminPermission(Permissions.VIEW_TUTORS);
        User t1 = new User("T1", "T", "X", "t@example.com", "pw",
            AccountStatus.ACTIVE, Role.TUTOR);
        Tutor tutor = new Tutor();
        tutor.setUserId("T1");
        tutor.setSubject("Math");

        when(userRepository.findAllByRole(Role.TUTOR)).thenReturn(List.of(t1));
        when(tutorRepository.findByUserId("T1")).thenReturn(Optional.of(tutor));

        var result = service.viewTutors("A1");
        assertEquals(1, result.size());
        assertEquals("Math", result.get(0).getSubject());
    }

    @Test
    void testApproveTutor_UpdatesTutorAndClearsStagedProfile() {
        mockAdminPermission(Permissions.APPROVE_TUTOR);
        User tutorUser = new User("T1", "T", "X", "t@example.com", "pw",
            AccountStatus.UNVERIFIED, Role.TUTOR);
        Tutor tutor = new Tutor();
        tutor.setUserId("T1");
        tutor.setStagedProfile(null);

        when(userRepository.findById("T1")).thenReturn(Optional.of(tutorUser));
        when(tutorRepository.findByUserId("T1")).thenReturn(Optional.of(tutor));

        service.approveTutor("A1", "T1");

        verify(userRepository).save(tutorUser);
        verify(tutorRepository).save(tutor);
        assertNull(tutor.getRejectedReason());
    }

    @Test
    void testRejectTutor_SetsRejectedReason() {
        mockAdminPermission(Permissions.REJECT_TUTOR);
        User tutorUser = new User("T1", "T", "X", "t@example.com", "pw",
            AccountStatus.ACTIVE, Role.TUTOR);
        Tutor tutor = new Tutor();
        tutor.setUserId("T1");
        tutor.setPreviousStatus(AccountStatus.ACTIVE);

        when(userRepository.findById("T1")).thenReturn(Optional.of(tutorUser));
        when(tutorRepository.findByUserId("T1")).thenReturn(Optional.of(tutor));

        service.rejectTutor("A1", "T1", "bad profile");
        assertEquals("bad profile", tutor.getRejectedReason());
    }

    // --------------- Admin Management ----------------
    @Test
    void testGetAdminByUserId_ReturnsDTO() {
        User user = new User("A2", "A", "B", "a@b.com", "pw",
            AccountStatus.ACTIVE, Role.ADMIN);
        Admin ad = new Admin("id", "A2", List.of(Permissions.VIEW_ADMIN));
        when(userRepository.findById("A2")).thenReturn(Optional.of(user));
        when(adminRepository.findByUserId("A2")).thenReturn(Optional.of(ad));

        Optional<AdminDTO> result = service.getAdminByUserId("A2");
        assertTrue(result.isPresent());
        assertEquals("A2", result.get().getUserId());
    }

    @Test
    void testCreateAdmin_CallsMapperAndRepository() {
        AdminDTO dto = AdminDTO.builder().id("x").build();
        Admin entity = new Admin();
        when(adminMapper.toEntity(dto)).thenReturn(entity);
        when(adminRepository.save(entity)).thenReturn(entity);

        service.createAdmin(dto);
        verify(adminMapper).toDTO(entity);
    }

    @Test
    void testEditAdminRoles_Success() {
        mockAdminPermission(Permissions.EDIT_ADMIN_ROLES);
        Admin target = new Admin("id", "A2", new ArrayList<>());
        when(adminRepository.findByUserId("A2")).thenReturn(Optional.of(target));

        service.editAdminRoles("A1", "A2", List.of(Permissions.VIEW_TUTORS));
        assertTrue(target.getPermissions().contains(Permissions.VIEW_TUTORS));
    }

    // --------------- Dashboard ----------------
    @Test
    void testGetDashboardSummary_CalculatesMetrics() {
        mockAdminPermission(Permissions.VIEW_TUTORS, Permissions.VIEW_STUDENTS, Permissions.VIEW_ADMIN);

        User u1 = new User("1", "N", "N", "e@x.com", "pw",
            AccountStatus.ACTIVE, Role.TUTOR);
        when(userRepository.findAll()).thenReturn(List.of(u1));
        when(tutorRepository.count()).thenReturn(1L);
        when(studentRepository.count()).thenReturn(1L);
        when(adminRepository.count()).thenReturn(1L);
        when(userRepository.findAllByRole(any())).thenReturn(List.of(u1));

        // Properly mock transaction metrics
        AdminDashboardDTO.TransactionMetrics metrics = AdminDashboardDTO.TransactionMetrics.builder()
            .totalEarnings(100.0)
            .commissionCollected(10.0)
            .highestTransaction(new AdminDashboardDTO.TransactionSummary("desc", 100.0))
            .monthlyEarnings(List.of(new AdminDashboardDTO.MonthlyEarnings("Jan", 100.0)))
            .build();
        when(walletService.getTransactionMetrics()).thenReturn(metrics);

        AdminDashboardDTO dto = service.getDashboardSummary("A1");

        assertNotNull(dto);
        assertEquals(100.0, dto.getTransactionMetrics().getTotalEarnings());
    }


    // --------------- Booking ----------------
    @Test
    void testDeleteBooking_DelegatesToBookingService() {
        mockAdminPermission(Permissions.DELETE_BOOKING);
        BookingDTO dto = BookingDTO.builder().id("B1").build();
        when(bookingService.deleteBooking("B1")).thenReturn(dto);

        BookingDTO result = service.deleteBooking("A1", "B1");
        assertEquals("B1", result.getId());
    }

    // --------------- Permission Checks ----------------
    @Test
    void testCheckAdminWithPermission_ThrowsIfMissingPermission() {
        mockAdminPermission(Permissions.VIEW_STUDENTS);
        admin.setPermissions(List.of(Permissions.VIEW_STUDENTS)); // missing required one

        assertThrows(RuntimeException.class, () -> service.editAdminRoles("A1", "A2", List.of()));
    }

    @Test
    void testGetUserOrThrow_ThrowsIfRoleMismatch() {
        User u = new User("X", "F", "L", "e@x.com", "pw",
            AccountStatus.ACTIVE, Role.STUDENT);
        when(userRepository.findById("X")).thenReturn(Optional.of(u));

        assertThrows(InvocationTargetException.class, () -> {
            var m = AdminServiceImpl.class.getDeclaredMethod("getUserOrThrow", String.class, Role.class);
            m.setAccessible(true);
            m.invoke(service, "X", Role.TUTOR);
        });
    }
}
