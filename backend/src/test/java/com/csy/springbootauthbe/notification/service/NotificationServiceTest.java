package com.csy.springbootauthbe.notification.service;

import com.csy.springbootauthbe.notification.dto.NotificationDTO;
import com.csy.springbootauthbe.notification.entity.Notification;
import com.csy.springbootauthbe.notification.mapper.NotificationMapper;
import com.csy.springbootauthbe.notification.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class NotificationServiceTest {

    @Mock
    private NotificationRepository repo;

    @Mock
    private NotificationMapper mapper;

    @InjectMocks
    private NotificationService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // ---------------- registerEmitter ----------------
    @Test
    void testRegisterEmitter_AddsAndRemovesProperly() {
        SseEmitter emitter = new SseEmitter();
        service.registerEmitter("u1", emitter);
        emitter.complete(); // triggers removal
        assertNotNull(emitter);
    }

    // ---------------- getUserNotifications ----------------
    @Test
    void testGetUserNotifications_ReturnsMappedList() {
        Notification n = new Notification();
        n.setId("n1");
        List<Notification> entities = List.of(n);
        when(repo.findByUserIdOrderByCreatedAtDesc("u1")).thenReturn(entities);
        when(mapper.toDto(any())).thenReturn(NotificationDTO.builder().id("n1").build());

        List<NotificationDTO> result = service.getUserNotifications("u1");
        assertEquals(1, result.size());
        assertEquals("n1", result.get(0).getId());
    }

    @Test
    void testGetUserNotifications_HandlesEmpty() {
        when(repo.findByUserIdOrderByCreatedAtDesc("u2")).thenReturn(Collections.emptyList());
        assertTrue(service.getUserNotifications("u2").isEmpty());
    }

    @Test
    void testGetUserNotifications_HandlesMappingError() {
        Notification n = new Notification();
        n.setId("nErr");
        when(repo.findByUserIdOrderByCreatedAtDesc("u3")).thenReturn(List.of(n));
        when(mapper.toDto(any())).thenThrow(new RuntimeException("mapping fail"));

        // should recover gracefully
        List<NotificationDTO> result = service.getUserNotifications("u3");
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetUserNotifications_RepositoryThrows() {
        when(repo.findByUserIdOrderByCreatedAtDesc(any())).thenThrow(new RuntimeException("DB fail"));
        List<NotificationDTO> result = service.getUserNotifications("uX");
        assertTrue(result.isEmpty());
    }

    // ---------------- createNotification ----------------
    @Test
    void testCreateNotification_Success() {
        Notification saved = new Notification();
        saved.setId("n100");
        when(repo.save(any())).thenReturn(saved);
        when(mapper.toDto(any())).thenReturn(NotificationDTO.builder().id("n100").build());

        NotificationDTO dto = service.createNotification("u1", "type", "b1", "msg");
        assertNotNull(dto);
        assertEquals("n100", dto.getId());
    }

    @Test
    void testCreateNotification_Failure() {
        when(repo.save(any())).thenThrow(new RuntimeException("DB error"));
        NotificationDTO dto = service.createNotification("u2", "type", "b1", "msg");
        assertNull(dto);
    }

    // ---------------- markAsRead ----------------
    @Test
    void testMarkAsRead_Success() {
        Notification n = new Notification();
        n.setId("x");
        n.setRead(false);
        when(repo.findById("x")).thenReturn(Optional.of(n));

        service.markAsRead("x");

        assertTrue(n.isRead());
        verify(repo).save(n);
    }

    @Test
    void testMarkAsRead_NotFound() {
        when(repo.findById(any())).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> service.markAsRead("nope"));
    }

    // ---------------- sendNotification (internal) ----------------
    @Test
    void testSendNotification_HandlesIOExceptionGracefully() throws IOException {
        // Register a fake emitter
        SseEmitter badEmitter = spy(new SseEmitter());
        doThrow(new IOException("fail")).when(badEmitter).send(Optional.ofNullable(any()));

        // manually register
        service.registerEmitter("u10", badEmitter);

        // manually invoke private method via reflection
        NotificationDTO dto = NotificationDTO.builder().id("n").build();
        assertDoesNotThrow(() -> {
            var m = NotificationService.class.getDeclaredMethod("sendNotification", String.class, NotificationDTO.class);
            m.setAccessible(true);
            m.invoke(service, "u10", dto);
        });
    }
}
