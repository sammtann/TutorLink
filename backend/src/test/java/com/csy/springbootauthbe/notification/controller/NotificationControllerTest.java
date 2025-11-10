package com.csy.springbootauthbe.notification.controller;

import com.csy.springbootauthbe.notification.dto.NotificationDTO;
import com.csy.springbootauthbe.notification.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class NotificationControllerTest {

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private NotificationController controller;

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    // ---------------- streamNotifications ----------------
    @Test
    void testStreamNotifications_ReturnsEmitterAndRegistersService() throws Exception {
        String userId = "user123";

        mockMvc.perform(get("/api/v1/notifications/stream/" + userId)
                .accept(MediaType.TEXT_EVENT_STREAM))
            .andExpect(status().isOk());

        verify(notificationService, times(1)).registerEmitter(eq(userId), any(SseEmitter.class));
    }

    // ---------------- getNotifications ----------------
    @Test
    void testGetNotifications_ReturnsList() throws Exception {
        String userId = "u001";
        NotificationDTO dto = NotificationDTO.builder()
            .id("n1")
            .userId(userId)
            .type("booking_created")
            .message("Booking created successfully")
            .createdAt(LocalDateTime.now())
            .read(false)
            .build();

        when(notificationService.getUserNotifications(userId)).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/v1/notifications")
                .param("userId", userId)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].id").value("n1"))
            .andExpect(jsonPath("$[0].userId").value(userId))
            .andExpect(jsonPath("$[0].message").value("Booking created successfully"));

        verify(notificationService).getUserNotifications(userId);
    }

    // ---------------- markAsRead ----------------
    @Test
    void testMarkAsRead_CallsService() throws Exception {
        String id = "notif123";

        mockMvc.perform(put("/api/v1/notifications/" + id + "/read"))
            .andExpect(status().isOk());

        verify(notificationService).markAsRead(id);
    }
}
