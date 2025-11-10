package com.csy.springbootauthbe.booking.service;

import com.csy.springbootauthbe.booking.dto.BookingDTO;
import com.csy.springbootauthbe.booking.dto.BookingRequest;
import com.csy.springbootauthbe.booking.entity.Booking;
import com.csy.springbootauthbe.booking.mapper.BookingMapper;
import com.csy.springbootauthbe.booking.repository.BookingRepository;
import com.csy.springbootauthbe.notification.service.NotificationService;
import com.csy.springbootauthbe.user.repository.UserRepository;
import com.csy.springbootauthbe.wallet.service.WalletService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BookingServiceImplTest {

    @Mock private BookingRepository bookingRepository;
    @Mock private BookingMapper bookingMapper;
    @Mock private NotificationService notificationService;
    @Mock private WalletService walletService;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private BookingRequest request;
    private Booking booking;
    private BookingDTO bookingDTO;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        request = BookingRequest.builder()
            .tutorId("T1")
            .studentId("S1")
            .tutorName("Tutor Tan")
            .studentName("Student Lim")
            .lessonType("Math")
            .date(LocalDate.now().toString())
            .start("10:00")
            .end("11:00")
            .amount(BigDecimal.valueOf(50))
            .build();

        booking = Booking.builder()
            .id("B1")
            .tutorId("T1")
            .studentId("S1")
            .status("pending")
            .amount(BigDecimal.valueOf(50))
            .build();

        bookingDTO = BookingDTO.builder().id("B1").status("pending").build();
    }

    // ---------------- CREATE BOOKING ----------------

    @Test
    void testCreateBooking_Success() {
        when(bookingRepository.findByTutorIdAndDate(anyString(), anyString())).thenReturn(List.of());
        when(bookingRepository.findByStudentIdAndDate(anyString(), anyString())).thenReturn(List.of());
        when(bookingMapper.toEntity(any())).thenReturn(booking);
        when(bookingRepository.save(any())).thenReturn(booking);
        when(bookingMapper.toDto(any())).thenReturn(bookingDTO);

        BookingDTO result = bookingService.createBooking(request);

        assertEquals("B1", result.getId());
        verify(walletService).holdCredits(eq("S1"), any(), anyString());
        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    void testCreateBooking_ThrowsOverlapError() {
        Booking overlap = Booking.builder().status("confirmed").start("09:30").end("10:30").build();
        when(bookingRepository.findByTutorIdAndDate(any(), any())).thenReturn(List.of(overlap));

        assertThrows(RuntimeException.class, () -> bookingService.createBooking(request));
    }

    @Test
    void testCreateBooking_InvalidAmount() {
        request.setAmount(BigDecimal.ZERO);
        when(bookingRepository.findByTutorIdAndDate(any(), any())).thenReturn(List.of());
        when(bookingRepository.findByStudentIdAndDate(any(), any())).thenReturn(List.of());

        assertThrows(RuntimeException.class, () -> bookingService.createBooking(request));
    }

    // ---------------- ACCEPT BOOKING ----------------

    @Test
    void testAcceptBooking_Success() {
        booking.setStatus("pending");
        when(bookingRepository.findById("B1")).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any())).thenReturn(booking);
        when(bookingMapper.toDto(any())).thenReturn(bookingDTO);

        BookingDTO result = bookingService.acceptBooking("B1");

        assertEquals("B1", result.getId());
        verify(walletService).releaseToTutor(eq("S1"), eq("T1"), any(), eq("B1"));
        verify(bookingRepository).save(booking);
    }

    @Test
    void testAcceptBooking_InvalidStatus() {
        booking.setStatus("confirmed");
        when(bookingRepository.findById("B1")).thenReturn(Optional.of(booking));

        assertThrows(RuntimeException.class, () -> bookingService.acceptBooking("B1"));
    }

    // ---------------- CANCEL BOOKING ----------------

    @Test
    void testCancelBooking_RefundsStudent() {
        booking.setStatus("pending");
        when(bookingRepository.findById("B1")).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any())).thenReturn(booking);
        when(bookingMapper.toDto(any())).thenReturn(bookingDTO);

        BookingDTO result = bookingService.cancelBooking("B1", "S1");

        assertEquals("cancelled", booking.getStatus()); // updated internally
        verify(walletService).refundStudent(eq("S1"), any(), eq("B1"));
        assertNotNull(result);
    }

    @Test
    void testCancelBooking_AlreadyCancelled() {
        booking.setStatus("cancelled");
        when(bookingRepository.findById("B1")).thenReturn(Optional.of(booking));

        assertThrows(RuntimeException.class, () -> bookingService.cancelBooking("B1", "S1"));
    }

    // ---------------- GET BOOKINGS ----------------

    @Test
    void testGetBookingById() {
        when(bookingRepository.findById("B1")).thenReturn(Optional.of(booking));
        when(bookingMapper.toDto(any())).thenReturn(bookingDTO);

        BookingDTO result = bookingService.getBookingById("B1");
        assertEquals("B1", result.getId());
    }

    @Test
    void testGetBookingsForTutor() {
        when(bookingRepository.findByTutorIdAndDate(any(), any())).thenReturn(List.of(booking));
        when(bookingMapper.toDto(any())).thenReturn(bookingDTO);

        List<BookingDTO> result = bookingService.getBookingsForTutor("T1", "2025-11-02");
        assertEquals(1, result.size());
    }

    // ---------------- RESCHEDULE ----------------

    @Test
    void testRequestReschedule_Success() {
        BookingRequest newSlot = BookingRequest.builder()
            .tutorId("T1").studentId("S1").start("12:00").end("13:00").date(LocalDate.now().toString())
            .lessonType("Math").build();

        Booking current = Booking.builder().id("B1").status("confirmed").tutorId("T1").build();

        when(bookingRepository.findById("B1")).thenReturn(Optional.of(current));
        when(bookingRepository.findByTutorIdAndDate(any(), any())).thenReturn(List.of());
        when(bookingMapper.toEntity(any())).thenReturn(booking);
        when(bookingRepository.save(any())).thenReturn(booking);
        when(bookingMapper.toDto(any())).thenReturn(bookingDTO);

        BookingDTO result = bookingService.requestReschedule("B1", newSlot);
        assertEquals("B1", result.getId());
    }

    @Test
    void testRequestReschedule_ThrowsConflict() {
        BookingRequest newSlot = BookingRequest.builder()
            .tutorId("T1").studentId("S1").start("09:00").end("10:00").date(LocalDate.now().toString())
            .lessonType("Math").build();

        Booking current = Booking.builder().id("B1").status("confirmed").build();
        Booking overlap = Booking.builder().status("confirmed").start("08:30").end("09:30").build();

        when(bookingRepository.findById("B1")).thenReturn(Optional.of(current));
        when(bookingRepository.findByTutorIdAndDate(any(), any())).thenReturn(List.of(overlap));

        assertThrows(RuntimeException.class, () -> bookingService.requestReschedule("B1", newSlot));
    }

    @Test
    void testApproveReschedule_Success() {
        Booking newBooking = Booking.builder().id("NB").originalBookingId("OB").build();
        Booking oldBooking = Booking.builder().id("OB").status("confirmed").build();

        when(bookingRepository.findById("NB")).thenReturn(Optional.of(newBooking));
        when(bookingRepository.findById("OB")).thenReturn(Optional.of(oldBooking));
        when(bookingRepository.save(any())).thenReturn(newBooking);
        when(bookingMapper.toDto(any())).thenReturn(bookingDTO);

        BookingDTO result = bookingService.approveReschedule("NB");
        assertEquals("B1", result.getId());
    }

    @Test
    void testRejectReschedule_Success() {
        Booking newBooking = Booking.builder()
            .id("NB").status("on_hold").originalBookingId("OB")
            .tutorId("T1").studentId("S1")
            .lessonType("Math").tutorName("Tutor").studentName("Student").date("2025-11-02").build();

        Booking original = Booking.builder().id("OB").status("confirmed").build();

        when(bookingRepository.findById("NB")).thenReturn(Optional.of(newBooking));
        when(bookingRepository.findById("OB")).thenReturn(Optional.of(original));
        when(bookingMapper.toDto(any())).thenReturn(bookingDTO);

        BookingDTO result = bookingService.rejectReschedule("NB");
        assertNotNull(result);
        verify(notificationService, atLeastOnce()).createNotification(any(), any(), any(), any());
    }

    // ---------------- DELETE ----------------

    @Test
    void testDeleteBooking_Success() {
        when(bookingRepository.findById("B1")).thenReturn(Optional.of(booking));
        when(bookingMapper.toDto(any())).thenReturn(bookingDTO);

        BookingDTO result = bookingService.deleteBooking("B1");
        assertEquals("B1", result.getId());
        verify(bookingRepository).save(any(Booking.class));
    }
}
