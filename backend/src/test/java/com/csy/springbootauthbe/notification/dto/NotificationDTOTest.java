package com.csy.springbootauthbe.notification.dto;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class NotificationDTOTest {

    @Test
    void testBuilderAndGetters() {
        LocalDateTime now = LocalDateTime.now();
        NotificationDTO dto = NotificationDTO.builder()
            .id("1")
            .userId("u1")
            .type("booking_cancelled")
            .bookingId("b001")
            .message("Cancelled successfully")
            .read(true)
            .createdAt(now)
            .build();

        assertEquals("1", dto.getId());
        assertEquals("u1", dto.getUserId());
        assertEquals("b001", dto.getBookingId());
        assertTrue(dto.isRead());
        assertEquals(now, dto.getCreatedAt());
    }

    @Test
    void testNoArgsAndSetters() {
        NotificationDTO dto = new NotificationDTO();
        dto.setId("5");
        dto.setUserId("x");
        dto.setType("info");
        dto.setMessage("Message");
        dto.setRead(false);

        assertEquals("x", dto.getUserId());
        assertEquals("Message", dto.getMessage());
        assertFalse(dto.isRead());
    }
}
