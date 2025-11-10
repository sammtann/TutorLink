package com.csy.springbootauthbe.tutor.utils;

import com.csy.springbootauthbe.tutor.entity.Availability;
import com.csy.springbootauthbe.tutor.entity.QualificationFile;
import org.junit.jupiter.api.Test;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class TutorRequestTest {

    @Test
    void testBuilderAndGetters() {
        Availability availability = new Availability();
        QualificationFile qf = new QualificationFile();

        // ✅ Create a mock MultipartFile (non-null)
        MultipartFile mockFile = org.mockito.Mockito.mock(MultipartFile.class);

        TutorRequest req = TutorRequest.builder()
            .userId("T123")
            .subject("Mathematics")
            .hourlyRate(45.5)
            .description("Experienced tutor")
            .lessonType(List.of("Online", "Group"))
            .fileUploads(List.of(mockFile)) // ✅ use mock instead of null
            .qualifications(List.of(qf))
            .availability(Map.of("Monday", availability))
            .build();

        assertEquals("T123", req.getUserId());
        assertEquals("Mathematics", req.getSubject());
        assertEquals(45.5, req.getHourlyRate());
        assertEquals("Experienced tutor", req.getDescription());
        assertEquals(2, req.getLessonType().size());
        assertEquals(1, req.getFileUploads().size());
        assertEquals(1, req.getQualifications().size());
        assertEquals(1, req.getAvailability().size());
    }

    @Test
    void testNoArgsConstructorAndSetters() {
        TutorRequest req = new TutorRequest();
        req.setUserId("T234");
        req.setSubject("Science");
        req.setHourlyRate(30.0);
        req.setDescription("Beginner tutor");

        assertEquals("T234", req.getUserId());
        assertEquals("Science", req.getSubject());
        assertEquals(30.0, req.getHourlyRate());
        assertEquals("Beginner tutor", req.getDescription());
    }

    @Test
    void testEqualsHashCodeToString() {
        TutorRequest req1 = TutorRequest.builder().userId("T1").subject("Math").build();
        TutorRequest req2 = TutorRequest.builder().userId("T1").subject("Math").build();
        TutorRequest req3 = TutorRequest.builder().userId("T2").subject("Physics").build();

        assertEquals(req1, req2);
        assertNotEquals(req1, req3);
        assertEquals(req1.hashCode(), req2.hashCode());
        assertNotNull(req1.toString());
    }
}
