package com.csy.springbootauthbe.booking.entity;

import org.junit.jupiter.api.Test;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;


import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class BookingTest {

    @Test
    void testBookingEntityAnnotations() {
        // Check @Document annotation
        Document documentAnnotation = Booking.class.getAnnotation(Document.class);
        assertNotNull(documentAnnotation);
        assertEquals("bookings", documentAnnotation.collection());

        // Check @Field annotations
        try {
            java.lang.reflect.Field createdAtField = Booking.class.getDeclaredField("createdAt");
            java.lang.reflect.Field updatedAtField = Booking.class.getDeclaredField("updatedAt");

            Field createdAtAnnotation = createdAtField.getAnnotation(Field.class);
            assertNotNull(createdAtAnnotation);
            assertEquals("created_at", createdAtAnnotation.value());

            Field updatedAtAnnotation = updatedAtField.getAnnotation(Field.class);
            assertNotNull(updatedAtAnnotation);
            assertEquals("updated_at", updatedAtAnnotation.value());
        } catch (NoSuchFieldException e) {
            fail("Field not found: " + e.getMessage());
        }

        // Check @CreatedDate and @LastModifiedDate annotations
        try {
            java.lang.reflect.Field createdAtField = Booking.class.getDeclaredField("createdAt");
            assertNotNull(createdAtField.getAnnotation(CreatedDate.class));

            java.lang.reflect.Field updatedAtField = Booking.class.getDeclaredField("updatedAt");
            assertNotNull(updatedAtField.getAnnotation(LastModifiedDate.class));
        } catch (NoSuchFieldException e) {
            fail("Field not found: " + e.getMessage());
        }
    }

    @Test
    void testBookingEntityLombokMethods() {
        // Test Lombok-generated methods
        Booking booking = Booking.builder()
                .id("1")
                .tutorId("tutor1")
                .studentId("student1")
                .date("2023-10-01")
                .status("confirmed")
                .amount(BigDecimal.valueOf(100))
                .build();

        assertEquals("1", booking.getId());
        assertEquals("tutor1", booking.getTutorId());
        assertEquals("student1", booking.getStudentId());
        assertEquals("2023-10-01", booking.getDate());
        assertEquals("confirmed", booking.getStatus());
        assertEquals(BigDecimal.valueOf(100), booking.getAmount());
    }
}