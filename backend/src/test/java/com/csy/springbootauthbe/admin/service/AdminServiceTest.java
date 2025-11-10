package com.csy.springbootauthbe.admin.service;

import com.csy.springbootauthbe.admin.dto.*;
import com.csy.springbootauthbe.user.utils.UserResponse;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

class AdminServiceTest {

    private final AdminService service = Mockito.mock(AdminService.class);

    @Test
    void testAllDeclaredMethodsExist() {
        assertNotNull(service);
        Mockito.when(service.viewStudents("a1")).thenReturn(List.of(UserResponse.builder().id("s1").build()));
        Mockito.when(service.getAdminByUserId("u1")).thenReturn(Optional.of(AdminDTO.builder().id("1").build()));
        Mockito.when(service.getDashboardSummary("a1")).thenReturn(new AdminDashboardDTO());

        assertEquals(1, service.viewStudents("a1").size());
        assertTrue(service.getAdminByUserId("u1").isPresent());
        assertNotNull(service.getDashboardSummary("a1"));
    }
}
