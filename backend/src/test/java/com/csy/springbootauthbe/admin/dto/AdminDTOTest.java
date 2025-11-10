package com.csy.springbootauthbe.admin.dto;

import com.csy.springbootauthbe.admin.entity.Permissions;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AdminDTOTest {

    @Test
    void testBuilderAndFields() {
        AdminDTO dto = AdminDTO.builder()
            .id("A1")
            .userId("U1")
            .firstName("John")
            .lastName("Doe")
            .email("john.doe@example.com")
            .role("ADMIN")
            .status("ACTIVE")
            .permissions(List.of(
                Permissions.VIEW_STUDENTS,
                Permissions.SUSPEND_TUTOR,
                Permissions.DELETE_ADMIN))
            .build();

        assertEquals("A1", dto.getId());
        assertEquals("U1", dto.getUserId());
        assertEquals("John", dto.getFirstName());
        assertEquals("Doe", dto.getLastName());
        assertEquals("john.doe@example.com", dto.getEmail());
        assertEquals("ADMIN", dto.getRole());
        assertEquals("ACTIVE", dto.getStatus());
        assertEquals(3, dto.getPermissions().size());
        assertTrue(dto.getPermissions().contains(Permissions.VIEW_STUDENTS));
        assertTrue(dto.toString().contains("john.doe@example.com"));
    }

    @Test
    void testNoArgsConstructorAndSetters() {
        AdminDTO dto = new AdminDTO();
        dto.setId("A2");
        dto.setUserId("U2");
        dto.setFirstName("Alice");
        dto.setLastName("Wong");
        dto.setEmail("alice.wong@example.com");
        dto.setRole("SUPER_ADMIN");
        dto.setStatus("SUSPENDED");
        dto.setPermissions(List.of(Permissions.CREATE_ADMIN, Permissions.DELETE_TUTOR));

        assertEquals("A2", dto.getId());
        assertEquals("U2", dto.getUserId());
        assertEquals("Alice", dto.getFirstName());
        assertEquals("Wong", dto.getLastName());
        assertEquals("alice.wong@example.com", dto.getEmail());
        assertEquals("SUPER_ADMIN", dto.getRole());
        assertEquals("SUSPENDED", dto.getStatus());
        assertEquals(2, dto.getPermissions().size());
        assertTrue(dto.getPermissions().contains(Permissions.CREATE_ADMIN));
    }

    @Test
    void testAllArgsConstructor() {
        List<Permissions> perms = List.of(Permissions.VIEW_ADMIN, Permissions.EDIT_ADMIN_ROLES);

        AdminDTO dto = new AdminDTO(
            "A3", "U3", "Bob", "Tan", "bob.tan@example.com",
            "ADMIN", "ACTIVE", perms);

        assertEquals("A3", dto.getId());
        assertEquals("U3", dto.getUserId());
        assertEquals("Bob", dto.getFirstName());
        assertEquals("Tan", dto.getLastName());
        assertEquals("bob.tan@example.com", dto.getEmail());
        assertEquals("ADMIN", dto.getRole());
        assertEquals("ACTIVE", dto.getStatus());
        assertEquals(perms, dto.getPermissions());
    }

    @Test
    void testEqualsAndHashCode() {
        List<Permissions> perms = List.of(Permissions.VIEW_TUTORS, Permissions.DELETE_BOOKING);

        AdminDTO dto1 = AdminDTO.builder()
            .id("A4").userId("U4")
            .firstName("Eve").lastName("Lim")
            .email("eve.lim@example.com")
            .role("ADMIN").status("ACTIVE")
            .permissions(perms)
            .build();

        AdminDTO dto2 = AdminDTO.builder()
            .id("A4").userId("U4")
            .firstName("Eve").lastName("Lim")
            .email("eve.lim@example.com")
            .role("ADMIN").status("ACTIVE")
            .permissions(perms)
            .build();

        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
    }

    @Test
    void testToStringContainsKeyFields() {
        AdminDTO dto = new AdminDTO(
            "A5", "U5", "Leo", "Tan",
            "leo.tan@example.com", "ADMIN", "ACTIVE",
            List.of(Permissions.VIEW_TUTORS));

        String str = dto.toString();
        assertTrue(str.contains("Leo"));
        assertTrue(str.contains("ADMIN"));
        assertTrue(str.contains("ACTIVE"));
        assertTrue(str.contains("VIEW_TUTORS"));
    }
}
