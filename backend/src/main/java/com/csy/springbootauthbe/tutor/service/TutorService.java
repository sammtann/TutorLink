package com.csy.springbootauthbe.tutor.service;

import com.csy.springbootauthbe.student.dto.StudentDTO;
import com.csy.springbootauthbe.tutor.dto.TutorDTO;
import com.csy.springbootauthbe.tutor.entity.Review;
import com.csy.springbootauthbe.tutor.utils.TutorRequest;
import com.csy.springbootauthbe.tutor.utils.TutorResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Optional;

public interface TutorService {
    TutorDTO createTutor(TutorDTO tutorDTO);
    Optional<TutorDTO> getTutorByUserId(String userId);
    void deleteTutor(String userId);
    TutorResponse updateTutor(String userId, TutorRequest updateRequest) throws NoSuchAlgorithmException, IOException;
    TutorDTO updateProfilePicture(String tutorId, MultipartFile file);

    TutorDTO addReview(String tutorId, String bookingId, String studentName, int rating, String comment);

    List<Review> getTutorReviewsByUserId(String userId);
}
