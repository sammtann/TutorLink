package com.csy.springbootauthbe.booking.dto;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.math.BigDecimal;

class BookingDTOTest {

    @Test
    void testBuilderAndFields() {
        BookingDTO dto = BookingDTO.builder()
            .id("b123")
            .tutorId("t101")
            .tutorName("John Doe")
            .studentId("s505")
            .studentName("Jane Lee")
            .lessonType("Mathematics")
            .date("2025-11-02")
            .start("10:00")
            .end("11:00")
            .status("CONFIRMED")
            .amount(BigDecimal.valueOf(120.00))
            .build();

        assertEquals("b123", dto.getId());
        assertEquals("t101", dto.getTutorId());
        assertEquals("John Doe", dto.getTutorName());
        assertEquals("s505", dto.getStudentId());
        assertEquals("Jane Lee", dto.getStudentName());
        assertEquals("Mathematics", dto.getLessonType());
        assertEquals("2025-11-02", dto.getDate());
        assertEquals("10:00", dto.getStart());
        assertEquals("11:00", dto.getEnd());
        assertEquals("CONFIRMED", dto.getStatus());
        assertEquals(BigDecimal.valueOf(120.00), dto.getAmount());
        assertTrue(dto.toString().contains("Mathematics"));
    }

    @Test
    void testNoArgsConstructorAndSetters() {
        BookingDTO dto = new BookingDTO();
        dto.setId("b200");
        dto.setTutorId("t200");
        dto.setTutorName("Alex");
        dto.setStudentId("s200");
        dto.setStudentName("Mira");
        dto.setLessonType("Science");
        dto.setDate("2025-12-01");
        dto.setStart("08:00");
        dto.setEnd("09:00");
        dto.setStatus("PENDING");
        dto.setAmount(BigDecimal.valueOf(99.99));

        assertEquals("b200", dto.getId());
        assertEquals("Alex", dto.getTutorName());
        assertEquals("Mira", dto.getStudentName());
        assertEquals("Science", dto.getLessonType());
        assertEquals("PENDING", dto.getStatus());
        assertEquals(BigDecimal.valueOf(99.99), dto.getAmount());
    }

    @Test
    void testEqualsAndHashCode() {
        BookingDTO dto1 = BookingDTO.builder().id("b1").tutorId("t1").status("CONFIRMED").build();
        BookingDTO dto2 = BookingDTO.builder().id("b1").tutorId("t1").status("CONFIRMED").build();

        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
    }
}
