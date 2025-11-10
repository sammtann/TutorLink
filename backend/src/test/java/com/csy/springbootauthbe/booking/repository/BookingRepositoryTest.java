package com.csy.springbootauthbe.booking.repository;

import com.csy.springbootauthbe.booking.entity.Booking;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BookingRepositoryTest {

    @Mock
    private BookingRepository bookingRepository;

    private Booking booking;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        booking = new Booking();
        booking.setId("b1");
        booking.setTutorId("t1");
        booking.setStudentId("s1");
        booking.setDate("2025-11-02");
        booking.setStatus("CONFIRMED");
    }

    @Test
    void testFindByTutorIdAndDate() {
        when(bookingRepository.findByTutorIdAndDate("t1", "2025-11-02"))
            .thenReturn(List.of(booking));

        var result = bookingRepository.findByTutorIdAndDate("t1", "2025-11-02");

        assertEquals(1, result.size());
        assertEquals("t1", result.get(0).getTutorId());
        verify(bookingRepository).findByTutorIdAndDate("t1", "2025-11-02");
    }

    @Test
    void testFindByStudentId() {
        when(bookingRepository.findByStudentId("s1"))
            .thenReturn(List.of(booking));

        var result = bookingRepository.findByStudentId("s1");

        assertEquals("s1", result.get(0).getStudentId());
        verify(bookingRepository).findByStudentId("s1");
    }

    @Test
    void testFindBookingsByTutorIdAndDateRange() {
        when(bookingRepository.findBookingsByTutorIdAndDateRange("t1", "2025-10-01", "2025-10-31"))
            .thenReturn(List.of(booking));

        var result = bookingRepository.findBookingsByTutorIdAndDateRange("t1", "2025-10-01", "2025-10-31");

        assertEquals(1, result.size());
        assertEquals("t1", result.get(0).getTutorId());
        verify(bookingRepository).findBookingsByTutorIdAndDateRange("t1", "2025-10-01", "2025-10-31");
    }

    @Test
    void testCountByTutorIdAndStatusAndDateBefore() {
        when(bookingRepository.countByTutorIdAndStatusAndDateBefore("t1", "CONFIRMED", "2025-11-01"))
            .thenReturn(3L);

        long count = bookingRepository.countByTutorIdAndStatusAndDateBefore("t1", "CONFIRMED", "2025-11-01");

        assertEquals(3L, count);
        verify(bookingRepository).countByTutorIdAndStatusAndDateBefore("t1", "CONFIRMED", "2025-11-01");
    }

    @Test
    void testFindTop5ByTutorIdAndStatusAndDateBeforeOrderByDateDesc() {
        when(bookingRepository.findTop5ByTutorIdAndStatusAndDateBeforeOrderByDateDesc("t1", "CONFIRMED", "2025-11-01"))
            .thenReturn(List.of(booking));

        var result = bookingRepository.findTop5ByTutorIdAndStatusAndDateBeforeOrderByDateDesc("t1", "CONFIRMED", "2025-11-01");

        assertFalse(result.isEmpty());
        assertEquals("t1", result.get(0).getTutorId());
        verify(bookingRepository).findTop5ByTutorIdAndStatusAndDateBeforeOrderByDateDesc("t1", "CONFIRMED", "2025-11-01");
    }

    @Test
    void testFindByTutorIdAndStatusInAndDateGreaterThanEqualOrderByDateAsc() {
        List<String> statuses = List.of("CONFIRMED", "PENDING");
        when(bookingRepository.findByTutorIdAndStatusInAndDateGreaterThanEqualOrderByDateAsc("t1", statuses, "2025-11-01"))
            .thenReturn(List.of(booking));

        var result = bookingRepository.findByTutorIdAndStatusInAndDateGreaterThanEqualOrderByDateAsc("t1", statuses, "2025-11-01");

        assertEquals(1, result.size());
        assertEquals("t1", result.get(0).getTutorId());
        verify(bookingRepository).findByTutorIdAndStatusInAndDateGreaterThanEqualOrderByDateAsc("t1", statuses, "2025-11-01");
    }

    @Test
    void testFindByStudentIdAndStatusInAndDateBeforeOrderByDateDesc() {
        List<String> statuses = List.of("CANCELLED", "COMPLETED");
        when(bookingRepository.findByStudentIdAndStatusInAndDateBeforeOrderByDateDesc("s1", statuses, "2025-10-31"))
            .thenReturn(List.of(booking));

        var result = bookingRepository.findByStudentIdAndStatusInAndDateBeforeOrderByDateDesc("s1", statuses, "2025-10-31");

        assertEquals("s1", result.get(0).getStudentId());
        verify(bookingRepository).findByStudentIdAndStatusInAndDateBeforeOrderByDateDesc("s1", statuses, "2025-10-31");
    }

    @Test
    void testFindByStudentIdAndDate() {
        when(bookingRepository.findByStudentIdAndDate("s1", "2025-11-02"))
            .thenReturn(List.of(booking));

        var result = bookingRepository.findByStudentIdAndDate("s1", "2025-11-02");

        assertEquals(1, result.size());
        assertEquals("2025-11-02", result.get(0).getDate());
        verify(bookingRepository).findByStudentIdAndDate("s1", "2025-11-02");
    }

    @Test
    void testFindById() {
        when(bookingRepository.findById("b1")).thenReturn(Optional.of(booking));

        Optional<Booking> found = bookingRepository.findById("b1");
        assertTrue(found.isPresent());
        assertEquals("b1", found.get().getId());
        verify(bookingRepository).findById("b1");
    }
}
