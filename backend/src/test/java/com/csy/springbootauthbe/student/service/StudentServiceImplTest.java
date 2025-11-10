package com.csy.springbootauthbe.student.service;

import com.csy.springbootauthbe.common.aws.AwsResponse;
import com.csy.springbootauthbe.common.aws.AwsService;
import com.csy.springbootauthbe.common.sequence.SequenceGeneratorService;
import com.csy.springbootauthbe.student.dto.StudentDTO;
import com.csy.springbootauthbe.student.entity.Student;
import com.csy.springbootauthbe.student.mapper.StudentMapper;
import com.csy.springbootauthbe.student.repository.StudentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.bson.Document;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudentServiceImplTest {

    @Mock StudentRepository repo;
    @Mock StudentMapper mapper;
    @Mock SequenceGeneratorService sequence;
    @Mock
    MongoTemplate mongoTemplate;
    @Mock
    AwsService awsService;
    @Mock org.springframework.web.multipart.MultipartFile file;

    @InjectMocks StudentServiceImpl service;

    @Test
    void createStudent_happyPath_assignsId_and_maps_and_saves() {
        StudentDTO in = new StudentDTO();
        in.setUserId("U1");

        Student mapped = new Student();
        Student saved = new Student();
        StudentDTO out = new StudentDTO();

        when(sequence.getNextStudentId()).thenReturn("S100");
        when(mapper.toEntity(in)).thenAnswer(inv -> {
            // emulate mapper behavior that uses generated id
            mapped.setId("S100");
            mapped.setUserId("U1");
            return mapped;
        });
        when(repo.save(mapped)).thenReturn(saved);
        when(mapper.toDTO(saved)).thenAnswer(inv -> {
            out.setId("S100");
            out.setUserId("U1");
            return out;
        });

        StudentDTO result = service.createStudent(in);

        assertEquals("S100", result.getId());
        assertEquals("U1", result.getUserId());
        verify(sequence).getNextStudentId();
        verify(mapper).toEntity(in);
        verify(repo).save(mapped);
        verify(mapper).toDTO(saved);
    }

    @Test
    void getStudentByUserId_found_returnsDto() {
        Student entity = new Student();
        entity.setUserId("U1");
        StudentDTO dto = new StudentDTO();
        dto.setUserId("U1");

        when(repo.findByUserId("U1")).thenReturn(Optional.of(entity));
        when(mapper.toDTO(entity)).thenReturn(dto);

        var result = service.getStudentByUserId("U1");

        assertTrue(result.isPresent());
        assertEquals("U1", result.get().getUserId());
    }

    @Test
    void getStudentByUserId_notFound_returnsEmpty() {
        when(repo.findByUserId("MISSING")).thenReturn(Optional.empty());

        var result = service.getStudentByUserId("MISSING");

        assertTrue(result.isEmpty());
        verify(mapper, never()).toDTO(any());
    }

    @Test
    void searchTutors_withFilters_buildsAggregationAndMapsResult() {
        var req = new com.csy.springbootauthbe.student.utils.TutorSearchRequest();
        req.setName("John");
        req.setSubject("Math");
        req.setMinPrice(10.0);
        req.setMaxPrice(100.0);
        req.setAvailability("MONDAY");

        org.bson.Document doc = new org.bson.Document("_id", new org.bson.types.ObjectId());
        doc.put("userId", "U1");
        doc.put("firstname", "John");
        doc.put("lastname", "Doe");
        doc.put("subject", "Math");
        doc.put("hourlyRate", 50.0);
        doc.put("profileImageUrl", "url");
        doc.put("description", "desc");
        doc.put("lessonType", java.util.List.of("Online"));
        doc.put("reviews", java.util.List.of(new org.bson.Document("studentName", "S1").append("rating", 5).append("comment", "Good")));
        doc.put("qualifications", java.util.List.of(new org.bson.Document("name", "Cert").append("type", "pdf").append("path", "p")));

        var aggResults = new org.springframework.data.mongodb.core.aggregation.AggregationResults<org.bson.Document>(
                java.util.List.of(doc), new org.bson.Document());

        // lenient() allows any Aggregation instance, so it won't throw PotentialStubbingProblem
        lenient().when(mongoTemplate.aggregate(
                any(org.springframework.data.mongodb.core.aggregation.Aggregation.class),
                eq("tutors"),
                eq(org.bson.Document.class))
        ).thenReturn(aggResults);

        var list = service.searchTutors(req);

        assertEquals(1, list.size());
        var dto = list.get(0);
        assertEquals("John", dto.getFirstName());
        assertEquals("Math", dto.getSubject());
        assertNotNull(dto.getReviews());
        assertNotNull(dto.getQualifications());
    }

    @Test
    void getTutorById_found_returnsMappedTutor() {
        org.bson.Document doc = new org.bson.Document("_id", new org.bson.types.ObjectId());
        doc.put("userId", "U1");
        doc.put("firstname", "Jane");
        doc.put("lastname", "Smith");
        doc.put("subject", "Physics");
        doc.put("hourlyRate", 45.0);

        org.springframework.data.mongodb.core.aggregation.AggregationResults<org.bson.Document> aggResults =
                new org.springframework.data.mongodb.core.aggregation.AggregationResults<>(
                        java.util.List.of(doc), new org.bson.Document());

        // ✅ Safe stubbing
        doReturn(aggResults).when(mongoTemplate)
                .aggregate(any(org.springframework.data.mongodb.core.aggregation.Aggregation.class),
                        eq("tutors"),
                        eq(org.bson.Document.class));

        var opt = service.getTutorById("507f1f77bcf86cd799439011");

        assertTrue(opt.isPresent());
        assertEquals("Physics", opt.get().getSubject());
    }



    @Test
    void getTutorById_notFound_returnsEmpty() {
        org.springframework.data.mongodb.core.aggregation.AggregationResults<org.bson.Document> aggResults =
                new org.springframework.data.mongodb.core.aggregation.AggregationResults<>(
                        java.util.Collections.emptyList(), new org.bson.Document());

        when(mongoTemplate.aggregate(
                any(org.springframework.data.mongodb.core.aggregation.Aggregation.class),
                eq("tutors"),
                eq(org.bson.Document.class))
        ).thenReturn(aggResults);

        var opt = service.getTutorById("507f1f77bcf86cd799439011");

        assertTrue(opt.isEmpty());
    }


    @Test
    void updateProfilePicture_deletesOldAndUploadsNew() {
        // given
        Student student = new Student();
        student.setUserId("S1");
        student.setProfileImageUrl("https://bucket.s3.amazonaws.com/oldKey");
        when(repo.findByUserId("S1")).thenReturn(Optional.of(student));

        AwsResponse uploadResponse = new AwsResponse();
        uploadResponse.setKey("newKey");
        uploadResponse.setHash("hash123");

        when(awsService.extractKeyFromUrl(anyString())).thenReturn("oldKey");
        when(awsService.uploadProfilePic(any(MultipartFile.class), eq("profilePicture"))).thenReturn(uploadResponse);
        doNothing().when(awsService).deleteProfilePic("oldKey");
        awsService.bucketName = "test-bucket";

        when(repo.save(any(Student.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // ✅ Return a non-null DTO from mapper
        StudentDTO mappedDto = new StudentDTO();
        mappedDto.setProfileImageUrl("https://test-bucket.s3.amazonaws.com/newKey");
        when(mapper.toDTO(any(Student.class))).thenReturn(mappedDto);

        MultipartFile mockFile = mock(MultipartFile.class);

        // when
        StudentDTO dto = service.updateProfilePicture("S1", mockFile);

        // then
        assertNotNull(dto);
        assertTrue(dto.getProfileImageUrl().contains("test-bucket.s3.amazonaws.com/newKey"));
        verify(awsService).deleteProfilePic("oldKey");
        verify(awsService).uploadProfilePic(any(MultipartFile.class), eq("profilePicture"));
        verify(repo).save(any(Student.class));
    }



    @Test
    void updateProfilePicture_studentNotFound_throws() {
        when(repo.findByUserId("X")).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> service.updateProfilePicture("X", file));
    }

    @Test
    void normalizeDay_returnsCorrectMappingAndNull() throws Exception {
        var method = com.csy.springbootauthbe.student.service.StudentServiceImpl.class
                .getDeclaredMethod("normalizeDay", String.class);
        method.setAccessible(true);

        assertEquals("Mon", method.invoke(service, "monday"));
        assertEquals("Tue", method.invoke(service, "TUESDAY"));
        assertNull(method.invoke(service, (Object) null));
    }
}
