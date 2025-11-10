package com.csy.springbootauthbe.tutor.service;

import com.csy.springbootauthbe.tutor.dto.TutorDTO;
import com.csy.springbootauthbe.tutor.entity.Tutor;
import com.csy.springbootauthbe.tutor.mapper.TutorMapper;
import com.csy.springbootauthbe.tutor.repository.TutorRepository;
import com.csy.springbootauthbe.user.entity.AccountStatus;
import com.csy.springbootauthbe.user.entity.User;
import com.csy.springbootauthbe.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.csy.springbootauthbe.common.aws.AwsResponse;
import com.csy.springbootauthbe.common.aws.AwsService;
import com.csy.springbootauthbe.tutor.entity.Review;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TutorServiceImplTest {

    @Mock UserRepository userRepository;
    @Mock TutorRepository tutorRepository;
    @Mock TutorMapper tutorMapper;
    @Mock
    AwsService awsService;

    @InjectMocks TutorServiceImpl tutorService;

    @Test
    void createTutor_mapsSavesAndReturnsDto() {
        // Arrange
        TutorDTO in = new TutorDTO();
        in.setUserId("U1");
        Tutor mapped = new Tutor();
        Tutor saved = new Tutor();
        TutorDTO out = new TutorDTO();

        when(tutorMapper.toEntity(in)).thenReturn(mapped);
        when(tutorRepository.save(mapped)).thenReturn(saved);
        when(tutorMapper.toDTO(saved)).thenReturn(out);

        // Act
        TutorDTO result = tutorService.createTutor(in);

        // Assert
        assertSame(out, result);
        verify(tutorMapper).toEntity(in);
        verify(tutorRepository).save(mapped);
        verify(tutorMapper).toDTO(saved);
    }

    @Test
    void getTutorByUserId_userMissing_returnsEmpty() {
        when(userRepository.findById("U404")).thenReturn(Optional.empty());
        when(tutorRepository.findByUserId("U404")).thenReturn(Optional.empty());

        var result = tutorService.getTutorByUserId("U404");

        assertTrue(result.isEmpty());
    }

    @Test
    void getTutorByUserId_userInactive_returnsEmpty() {
        User u = new User();
        u.setId("U2");
        u.setStatus(AccountStatus.SUSPENDED); // or INACTIVE

        when(userRepository.findById("U2")).thenReturn(Optional.of(u));
        when(tutorRepository.findByUserId("U2")).thenReturn(Optional.of(new Tutor()));

        var result = tutorService.getTutorByUserId("U2");

        assertTrue(result.isEmpty());
    }

    @Test
    void getTutorByUserId_ok_returnsDto() {
        User u = new User();
        u.setId("U1");
        u.setStatus(AccountStatus.ACTIVE);

        Tutor entity = new Tutor();
        TutorDTO dto = new TutorDTO();
        dto.setUserId("U1");

        when(userRepository.findById("U1")).thenReturn(Optional.of(u));
        when(tutorRepository.findByUserId("U1")).thenReturn(Optional.of(entity));
        when(tutorMapper.toDTO(entity)).thenReturn(dto);

        var result = tutorService.getTutorByUserId("U1");

        assertTrue(result.isPresent());
        assertEquals("U1", result.get().getUserId());
    }

    // ----------------------------------------------------------------------
    // updateProfilePicture()
    // ----------------------------------------------------------------------
    @Test
    void updateProfilePicture_replacesOldPictureAndSaves() throws IOException {
        Tutor tutor = new Tutor();
        tutor.setUserId("T1");
        tutor.setProfileImageUrl("https://tutorlink-s3.s3.us-east-1.amazonaws.com/profilePicture/oldKey");

        AwsResponse uploadRes = new AwsResponse();
        uploadRes.setKey("newKey");
        uploadRes.setHash("hash123");

        when(tutorRepository.findByUserId("T1")).thenReturn(Optional.of(tutor));
        when(awsService.extractKeyFromUrl(any())).thenReturn("oldKey");
        when(awsService.uploadProfilePic(any(), anyString())).thenReturn(uploadRes);
        when(tutorRepository.save(any(Tutor.class))).thenAnswer(inv -> inv.getArgument(0));
        when(tutorMapper.toDTO(any())).thenReturn(new TutorDTO());

        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.getOriginalFilename()).thenReturn("pic.jpg");
        when(mockFile.getBytes()).thenReturn("data".getBytes());

        TutorDTO result = tutorService.updateProfilePicture("T1", mockFile);

        assertNotNull(result);
        verify(awsService).deleteProfilePic("oldKey");
        verify(awsService).uploadProfilePic(mockFile, "profilePicture");
        verify(tutorRepository).save(any(Tutor.class));
    }

    @Test
    void updateProfilePicture_studentNotFound_throwsException() {
        when(tutorRepository.findByUserId("MISSING")).thenReturn(Optional.empty());
        MultipartFile mockFile = mock(MultipartFile.class);
        assertThrows(RuntimeException.class,
                () -> tutorService.updateProfilePicture("MISSING", mockFile));
    }

    // ----------------------------------------------------------------------
    // deleteTutor()
    // ----------------------------------------------------------------------
    @Test
    void deleteTutor_existingTutor_deletesSuccessfully() {
        Tutor tutor = new Tutor();
        tutor.setUserId("T1");
        when(tutorRepository.findByUserId("T1")).thenReturn(Optional.of(tutor));

        tutorService.deleteTutor("T1");

        verify(tutorRepository).delete(tutor);
    }

    @Test
    void deleteTutor_notFound_throwsUsernameNotFoundException() {
        when(tutorRepository.findByUserId("NF")).thenReturn(Optional.empty());
        assertThrows(org.springframework.security.core.userdetails.UsernameNotFoundException.class,
                () -> tutorService.deleteTutor("NF"));
    }

    // ----------------------------------------------------------------------
    // addReview()
    // ----------------------------------------------------------------------
    @Test
    void addReview_success_addsAndSaves() {
        Tutor tutor = new Tutor();
        tutor.setUserId("T1");
        tutor.setReviews(new ArrayList<>());

        when(tutorRepository.findByUserId("T1")).thenReturn(Optional.of(tutor));
        when(tutorRepository.save(any(Tutor.class))).thenReturn(tutor);
        when(tutorMapper.toDTO(any(Tutor.class))).thenReturn(new TutorDTO());

        TutorDTO result = tutorService.addReview("T1", "B001", "Alice", 5, "Excellent!");

        assertNotNull(result);
        verify(tutorRepository).save(any(Tutor.class));
        assertEquals(1, tutor.getReviews().size());
    }

    @Test
    void addReview_alreadyExists_throwsRuntimeException() {
        Review existing = new Review();
        existing.setBookingId("B001");
        Tutor tutor = new Tutor();
        tutor.setUserId("T1");
        tutor.setReviews(new ArrayList<>(List.of(existing)));

        when(tutorRepository.findByUserId("T1")).thenReturn(Optional.of(tutor));

        assertThrows(RuntimeException.class,
                () -> tutorService.addReview("T1", "B001", "Alice", 5, "Excellent!"));
    }

    @Test
    void addReview_tutorNotFound_throwsException() {
        when(tutorRepository.findByUserId("NF")).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class,
                () -> tutorService.addReview("NF", "B001", "Alice", 5, "Great!"));
    }

    // ----------------------------------------------------------------------
    // getTutorReviewsByUserId()
    // ----------------------------------------------------------------------
    @Test
    void getTutorReviewsByUserId_returnsSortedList() {
        Review oldReview = new Review();
        oldReview.setCreatedAt(LocalDateTime.now().minusDays(2));
        Review newReview = new Review();
        newReview.setCreatedAt(LocalDateTime.now());

        Tutor tutor = new Tutor();
        tutor.setUserId("T1");
        tutor.setReviews(List.of(oldReview, newReview));

        when(tutorRepository.findByUserId("T1")).thenReturn(Optional.of(tutor));

        List<Review> result = tutorService.getTutorReviewsByUserId("T1");

        assertEquals(2, result.size());
        assertTrue(result.get(0).getCreatedAt().isAfter(result.get(1).getCreatedAt()));
    }

    @Test
    void getTutorReviewsByUserId_emptyReviews_returnsEmptyList() {
        Tutor tutor = new Tutor();
        tutor.setUserId("T1");
        tutor.setReviews(Collections.emptyList());

        when(tutorRepository.findByUserId("T1")).thenReturn(Optional.of(tutor));

        List<Review> result = tutorService.getTutorReviewsByUserId("T1");

        assertTrue(result.isEmpty());
    }

    @Test
    void getTutorReviewsByUserId_tutorNotFound_throwsException() {
        when(tutorRepository.findByUserId("NF")).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class,
                () -> tutorService.getTutorReviewsByUserId("NF"));
    }
}
