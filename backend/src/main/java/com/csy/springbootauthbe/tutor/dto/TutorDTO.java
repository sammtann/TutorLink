package com.csy.springbootauthbe.tutor.dto;

import com.csy.springbootauthbe.tutor.entity.Availability;
import com.csy.springbootauthbe.tutor.entity.QualificationFile;
import com.csy.springbootauthbe.tutor.entity.Review;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TutorDTO {
    private String id;
    private String userId;
    private Double hourlyRate;
    private String firstName;
    private String lastName;
    private String email;
    private String status;
    private List<QualificationFile> qualifications;
    private List<MultipartFile> files;
    private Map<String, Availability> availability;
    private String subject;
    private String profileImageUrl;
    private List<String> lessonType;
    private String description;
    private String rejectedReason;
    private TutorStagedProfileDTO stagedProfile;
    private List<Review> reviews;
}
