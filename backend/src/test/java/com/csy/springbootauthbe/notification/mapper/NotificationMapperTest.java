package com.csy.springbootauthbe.notification.mapper;

import com.csy.springbootauthbe.notification.dto.NotificationDTO;
import com.csy.springbootauthbe.notification.entity.Notification;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class NotificationMapperTest {

    private final NotificationMapperImpl mapper = new NotificationMapperImpl();

    @Test
    void testToDto_Valid() {
        LocalDateTime now = LocalDateTime.now();
        Notification entity = Notification.builder()
            .id("n1")
            .userId("u1")
            .type("booking")
            .bookingId("b1")
            .message("Hello")
            .read(true)
            .createdAt(now)
            .build();

        NotificationDTO dto = mapper.toDto(entity);
        assertNotNull(dto);
        assertEquals("n1", dto.getId());
        assertEquals("u1", dto.getUserId());
        assertTrue(dto.isRead());
        assertEquals(now, dto.getCreatedAt());
    }

    @Test
    void testToDto_NullReturnsNull() {
        assertNull(mapper.toDto(null));
    }

    @Test
    void testToEntity_Valid() {
        LocalDateTime now = LocalDateTime.now();
        NotificationDTO dto = NotificationDTO.builder()
            .id("1")
            .userId("u2")
            .type("reminder")
            .bookingId("b2")
            .message("Reminder text")
            .read(false)
            .createdAt(now)
            .build();

        Notification entity = mapper.toEntity(dto);
        assertNotNull(entity);
        assertEquals("u2", entity.getUserId());
        assertFalse(entity.isRead());
        assertEquals(now, entity.getCreatedAt());
    }

    @Test
    void testToEntity_NullReturnsNull() {
        assertNull(mapper.toEntity(null));
    }
}
