package com.csy.springbootauthbe.booking.mapper;

import com.csy.springbootauthbe.booking.dto.BookingDTO;
import com.csy.springbootauthbe.booking.dto.BookingRequest;
import com.csy.springbootauthbe.booking.entity.Booking;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class BookingMapperImplTest {

    private BookingMapperImpl mapper;

    @BeforeEach
    void setUp() {
        mapper = new BookingMapperImpl();
    }

    @Test
    void testToEntity_WhenRequestIsValid() {
        BookingRequest request = BookingRequest.builder()
            .lessonType("Math")
            .tutorId("T1")
            .studentId("S1")
            .tutorName("Tutor Tan")
            .studentName("Student Lim")
            .date("2025-11-03")
            .start("10:00")
            .end("11:00")
            .amount(BigDecimal.valueOf(50.5))
            .build();

        Booking entity = mapper.toEntity(request);

        assertNotNull(entity);
        assertEquals("Math", entity.getLessonType());
        assertEquals("T1", entity.getTutorId());
        assertEquals("S1", entity.getStudentId());
        assertEquals("Tutor Tan", entity.getTutorName());
        assertEquals("Student Lim", entity.getStudentName());
        assertEquals("2025-11-03", entity.getDate());
        assertEquals("10:00", entity.getStart());
        assertEquals("11:00", entity.getEnd());
        assertEquals(BigDecimal.valueOf(50.5), entity.getAmount());
    }

    @Test
    void testToEntity_WhenRequestIsNull() {
        Booking entity = mapper.toEntity(null);
        assertNull(entity);
    }

    @Test
    void testToDto_WhenBookingIsValid() {
        Booking booking = Booking.builder()
            .id("B1")
            .tutorId("T1")
            .tutorName("Tutor Tan")
            .studentId("S1")
            .studentName("Student Lim")
            .lessonType("Science")
            .date("2025-11-05")
            .start("14:00")
            .end("15:00")
            .status("CONFIRMED")
            .amount(BigDecimal.valueOf(75.25))
            .build();

        BookingDTO dto = mapper.toDto(booking);

        assertNotNull(dto);
        assertEquals("B1", dto.getId());
        assertEquals("T1", dto.getTutorId());
        assertEquals("Tutor Tan", dto.getTutorName());
        assertEquals("S1", dto.getStudentId());
        assertEquals("Student Lim", dto.getStudentName());
        assertEquals("Science", dto.getLessonType());
        assertEquals("2025-11-05", dto.getDate());
        assertEquals("14:00", dto.getStart());
        assertEquals("15:00", dto.getEnd());
        assertEquals("CONFIRMED", dto.getStatus());
        assertEquals(BigDecimal.valueOf(75.25), dto.getAmount());
    }

    @Test
    void testToDto_WhenBookingIsNull() {
        BookingDTO dto = mapper.toDto(null);
        assertNull(dto);
    }

    @Test
    void testToEntityAndToDto_RoundTripConsistency() {
        BookingRequest request = BookingRequest.builder()
            .lessonType("English")
            .tutorId("T2")
            .studentId("S2")
            .tutorName("Tutor Lee")
            .studentName("Student Ong")
            .date("2025-11-06")
            .start("09:00")
            .end("10:00")
            .amount(BigDecimal.valueOf(100))
            .build();

        Booking entity = mapper.toEntity(request);
        BookingDTO dto = mapper.toDto(entity);

        assertEquals(entity.getTutorId(), dto.getTutorId());
        assertEquals(entity.getStudentId(), dto.getStudentId());
        assertEquals(entity.getLessonType(), dto.getLessonType());
        assertEquals(entity.getAmount(), dto.getAmount());
    }
}
