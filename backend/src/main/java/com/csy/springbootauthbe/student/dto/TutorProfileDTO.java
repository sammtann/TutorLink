package com.csy.springbootauthbe.student.dto;

import com.csy.springbootauthbe.tutor.entity.QualificationFile;
import com.csy.springbootauthbe.tutor.entity.Review;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class TutorProfileDTO {
    private String id;
    private String userId;
    private String firstName;
    private String lastName;
    private String subject;
    private Double hourlyRate;
    private Map<String, Object> availability; // you can refine this later
    private String description;
    private String profileImageUrl;
    private List<String> lessonType;
    private List<QualificationFile> qualifications;
    private List<Review> reviews;
}

