package com.csy.springbootauthbe.admin.entity;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class AdminTest {

    @Test
    void testEntityFields() {
        List<Permissions> perms = List.of(Permissions.VIEW_TUTORS);
        Admin admin = Admin.builder()
            .id("1")
            .userId("u1")
            .permissions(perms)
            .build();

        assertEquals("u1", admin.getUserId());
        assertEquals(Permissions.VIEW_TUTORS, admin.getPermissions().get(0));
    }
}
