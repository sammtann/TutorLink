package com.csy.springbootauthbe.booking.controller;

import com.csy.springbootauthbe.booking.dto.BookingDTO;
import com.csy.springbootauthbe.booking.dto.BookingRequest;
import com.csy.springbootauthbe.booking.dto.RecentBookingResponse;
import com.csy.springbootauthbe.booking.service.BookingService;
import com.csy.springbootauthbe.student.controller.StudentController;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
    controllers = BookingController.class,
    excludeAutoConfiguration = {
        org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
        org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class
    }
)
@ContextConfiguration(classes = BookingController.class)
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookingService bookingService;

    @Test
    void testCreateBooking() throws Exception {
        BookingDTO bookingDTO = new BookingDTO();
        Mockito.when(bookingService.createBooking(any(BookingRequest.class))).thenReturn(bookingDTO);

        BookingRequest request = new BookingRequest();
        mockMvc.perform(post("/api/v1/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))) // Use the injected ObjectMapper instance
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void testGetBookingsForTutor() throws Exception {
        Mockito.when(bookingService.getBookingsForTutor(anyString(), anyString()))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/bookings/tutor/tutorId")
                .param("date", "2023-10-01"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void testGetBookingsForTutorInRange() throws Exception {
        Mockito.when(bookingService.getBookingsForTutorBetweenDates(anyString(), anyString(), anyString()))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/bookings/tutor/range/tutorId")
                .param("startDate", "2023-10-01")
                .param("endDate", "2023-10-10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void testGetRecentPastBookings() throws Exception {
        Mockito.when(bookingService.getRecentPastBookings(anyString()))
                .thenReturn(new RecentBookingResponse());

        mockMvc.perform(get("/api/v1/bookings/tutor/range/tutorId/past"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void testGetUpcomingBookings() throws Exception {
        Mockito.when(bookingService.getUpcomingBookings(anyString()))
                .thenReturn(new RecentBookingResponse());

        mockMvc.perform(get("/api/v1/bookings/tutor/range/tutorId/upcoming"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void testGetBookingsForStudent() throws Exception {
        Mockito.when(bookingService.getBookingsForStudent(anyString()))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/bookings/student/studentId"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void testCancelBooking() throws Exception {
        BookingDTO bookingDTO = new BookingDTO();
        Mockito.when(bookingService.cancelBooking(anyString(), anyString())).thenReturn(bookingDTO);

        mockMvc.perform(put("/api/v1/bookings/bookingId/cancel/currentUserId"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void testAcceptBooking() throws Exception {
        BookingDTO bookingDTO = new BookingDTO();
        Mockito.when(bookingService.acceptBooking(anyString())).thenReturn(bookingDTO);

        mockMvc.perform(put("/api/v1/bookings/bookingId/accept"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void testGetBookingById() throws Exception {
        BookingDTO bookingDTO = new BookingDTO();
        Mockito.when(bookingService.getBookingById(anyString())).thenReturn(bookingDTO);

        mockMvc.perform(get("/api/v1/bookings/bookingId"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void testRequestReschedule() throws Exception {
        BookingDTO bookingDTO = new BookingDTO();
        Mockito.when(bookingService.requestReschedule(anyString(), any(BookingRequest.class)))
                .thenReturn(bookingDTO);

        BookingRequest request = new BookingRequest();
        mockMvc.perform(post("/api/v1/bookings/bookingId/reschedule")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void testApproveReschedule() throws Exception {
        BookingDTO bookingDTO = new BookingDTO();
        Mockito.when(bookingService.approveReschedule(anyString())).thenReturn(bookingDTO);

        mockMvc.perform(put("/api/v1/bookings/reschedule/newBookingId/approve"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void testRejectReschedule() throws Exception {
        BookingDTO bookingDTO = new BookingDTO();
        Mockito.when(bookingService.rejectReschedule(anyString())).thenReturn(bookingDTO);

        mockMvc.perform(put("/api/v1/bookings/reschedule/newBookingId/reject"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void testGetStudentPastSessions() throws Exception {
        Mockito.when(bookingService.getPastSessionsForStudent(anyString()))
                .thenReturn(new RecentBookingResponse());

        mockMvc.perform(get("/api/v1/bookings/student/studentId/past"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }
}