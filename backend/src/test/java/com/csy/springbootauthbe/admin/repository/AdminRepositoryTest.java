package com.csy.springbootauthbe.admin.repository;

import com.csy.springbootauthbe.admin.entity.Admin;
import com.csy.springbootauthbe.admin.entity.Permissions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import java.util.Optional;
import java.util.List;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@DataMongoTest
class AdminRepositoryTest {

    @MockBean
    private AdminRepository adminRepository;

    @Test
    void testFindByUserId() {
        Admin admin = Admin.builder().id("1").userId("u1")
            .permissions(List.of(Permissions.VIEW_STUDENTS))
            .build();
        when(adminRepository.findByUserId("u1")).thenReturn(Optional.of(admin));

        Optional<Admin> found = adminRepository.findByUserId("u1");
        assertTrue(found.isPresent());
        assertEquals("u1", found.get().getUserId());
        verify(adminRepository, times(1)).findByUserId("u1");
    }
}
