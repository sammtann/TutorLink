package com.csy.springbootauthbe.admin.util;

import com.csy.springbootauthbe.admin.entity.Permissions;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AdminResponseTest {

    @Test
    void testBuilderAndFields() {
        AdminResponse response = AdminResponse.builder()
            .permissions(List.of(
                Permissions.VIEW_STUDENTS,
                Permissions.SUSPEND_TUTOR))
            .build();

        assertNotNull(response);
        assertEquals(2, response.getPermissions().size());
        assertTrue(response.getPermissions().contains(Permissions.VIEW_STUDENTS));
        assertTrue(response.toString().contains("VIEW_STUDENTS"));
    }

    @Test
    void testAllArgsConstructor() {
        List<Permissions> perms = List.of(Permissions.DELETE_ADMIN, Permissions.VIEW_TUTORS);
        AdminResponse response = new AdminResponse(perms);

        assertEquals(perms, response.getPermissions());
    }

    @Test
    void testNoArgsConstructorAndSetters() {
        AdminResponse response = new AdminResponse();
        response.setPermissions(List.of(Permissions.CREATE_ADMIN));

        assertEquals(1, response.getPermissions().size());
        assertEquals(Permissions.CREATE_ADMIN, response.getPermissions().get(0));
    }

    @Test
    void testEqualsAndHashCode() {
        List<Permissions> perms = List.of(Permissions.VIEW_ADMIN);
        AdminResponse r1 = AdminResponse.builder().permissions(perms).build();
        AdminResponse r2 = AdminResponse.builder().permissions(perms).build();

        assertEquals(r1, r2);
        assertEquals(r1.hashCode(), r2.hashCode());
    }

    @Test
    void testToStringContainsEnumValues() {
        AdminResponse response = AdminResponse.builder()
            .permissions(List.of(Permissions.SUPER_ADMIN))
            .build();

        String str = response.toString();
        assertTrue(str.contains("SUPER_ADMIN"));
    }
}
