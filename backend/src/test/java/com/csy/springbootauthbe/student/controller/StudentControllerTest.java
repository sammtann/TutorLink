package com.csy.springbootauthbe.student.controller;

import com.csy.springbootauthbe.student.dto.StudentDTO;
import com.csy.springbootauthbe.student.dto.TutorProfileDTO;
import com.csy.springbootauthbe.student.service.StudentService;
import com.csy.springbootauthbe.student.utils.TutorSearchRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = StudentController.class,
        excludeAutoConfiguration = {
                org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
                org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class
        }
)
@AutoConfigureMockMvc(addFilters = false)
class StudentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StudentService studentService;

    // ✅ Mock security-related beans so Spring context doesn’t fail
    @MockBean
    private com.csy.springbootauthbe.config.JWTAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private com.csy.springbootauthbe.config.JWTService jwtService;

    @MockBean
    private com.csy.springbootauthbe.common.wrapper.UserDetailsServiceWrapper userDetailsServiceWrapper;

    @Test
    void getStudentByUserId_ok_returns200() throws Exception {
        var dto = new StudentDTO();
        dto.setId("S1");
        dto.setUserId("U1");

        when(studentService.getStudentByUserId("U1")).thenReturn(Optional.of(dto));

        mockMvc.perform(get("/api/v1/students/by-user/{userId}", "U1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"));
    }

    @Test
    void getStudentByUserId_notFound_returns404() throws Exception {
        when(studentService.getStudentByUserId("U404")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/students/by-user/{userId}", "U404"))
                .andExpect(status().isNotFound());
    }

    // ----------------------------------------------------------------------
    // POST /search
    // ----------------------------------------------------------------------
    @Test
    void searchTutors_returnsList() throws Exception {
        TutorProfileDTO tutor = new TutorProfileDTO();
        tutor.setId("T1");
        tutor.setFirstName("John");
        tutor.setLastName("Doe");
        tutor.setSubject("Math");

        when(studentService.searchTutors(any(TutorSearchRequest.class)))
                .thenReturn(List.of(tutor));

        String body = """
                {"name":"John","subject":"Math","minPrice":10.0,"maxPrice":50.0,"availability":"MONDAY"}
                """;

        mockMvc.perform(post("/api/v1/students/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].firstName").value("John"))
                .andExpect(jsonPath("$[0].subject").value("Math"));
    }

    // ----------------------------------------------------------------------
    // GET /tutors/{id}
    // ----------------------------------------------------------------------
    @Test
    void getTutorById_found_returns200() throws Exception {
        TutorProfileDTO dto = new TutorProfileDTO();
        dto.setId("T1");
        dto.setFirstName("Jane");
        dto.setSubject("Physics");

        when(studentService.getTutorById("T1")).thenReturn(Optional.of(dto));

        mockMvc.perform(get("/api/v1/students/tutors/{id}", "T1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.firstName").value("Jane"))
                .andExpect(jsonPath("$.subject").value("Physics"));
    }

    @Test
    void getTutorById_notFound_returns404() throws Exception {
        when(studentService.getTutorById("T404")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/students/tutors/{id}", "T404"))
                .andExpect(status().isNotFound());
    }

    // ----------------------------------------------------------------------
    // POST /{id}/profile-picture
    // ----------------------------------------------------------------------
    @Test
    void uploadProfilePicture_returns200() throws Exception {
        StudentDTO dto = new StudentDTO();
        dto.setId("S1");
        dto.setProfileImageUrl("https://test-bucket.s3.amazonaws.com/newKey");

        when(studentService.updateProfilePicture(any(), any())).thenReturn(dto);

        MockMultipartFile file = new MockMultipartFile(
                "file", "pic.jpg", "image/jpeg", "dummy".getBytes());

        mockMvc.perform(multipart("/api/v1/students/{id}/profile-picture", "S1")
                        .file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.profileImageUrl").value("https://test-bucket.s3.amazonaws.com/newKey"));
    }
}
