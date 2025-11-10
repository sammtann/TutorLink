package com.csy.springbootauthbe.admin.entity;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PermissionsTest {

    @Test
    void testEnumValues() {
        assertNotNull(Permissions.valueOf("VIEW_STUDENTS"));
        assertTrue(Permissions.values().length > 5);
    }
}
