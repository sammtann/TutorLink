package com.csy.springbootauthbe.notification.entity;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class NotificationEntityTest {

    @Test
    void testBuilderAndFields() {
        LocalDateTime now = LocalDateTime.now();
        Notification n = Notification.builder()
            .id("n001")
            .userId("u123")
            .type("booking")
            .bookingId("b123")
            .message("Lesson confirmed")
            .read(true)
            .createdAt(now)
            .build();

        assertEquals("u123", n.getUserId());
        assertTrue(n.isRead());
        assertEquals("Lesson confirmed", n.getMessage());
    }

    @Test
    void testNoArgsAndDefaultValues() {
        Notification n = new Notification();
        n.setId("2");
        n.setUserId("u2");
        assertNotNull(n.getCreatedAt());
        assertFalse(n.isRead());
    }
}
