package com.csy.springbootauthbe.booking.service;

import com.csy.springbootauthbe.booking.dto.BookingDTO;
import com.csy.springbootauthbe.booking.dto.BookingRequest;
import com.csy.springbootauthbe.booking.dto.RecentBookingResponse;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

public interface BookingService {
    BookingDTO createBooking(BookingRequest dto);
    List<BookingDTO> getBookingsForTutor(String tutorId, String date);
    List<BookingDTO> getBookingsForStudent(String studentId);
    BookingDTO cancelBooking(String bookingId, String currentUserId);
    BookingDTO acceptBooking(String bookingId);
    BookingDTO getBookingById(String bookingId);
    List<BookingDTO> getBookingsForTutorBetweenDates(String tutorId, String startDate, String endDate);
    RecentBookingResponse getRecentPastBookings(String tutorId);
    RecentBookingResponse getUpcomingBookings(String tutorId);
    BookingDTO approveReschedule(String newBookingId);
    BookingDTO requestReschedule(String bookingId, BookingRequest newSlotRequest);
    BookingDTO rejectReschedule(String newBookingId);

    RecentBookingResponse getPastSessionsForStudent(String studentId);
    BookingDTO deleteBooking(String bookingId);
}