package com.csy.springbootauthbe.tutor.entity;

import com.csy.springbootauthbe.tutor.dto.TutorStagedProfileDTO;
import com.csy.springbootauthbe.user.entity.AccountStatus;
import com.mongodb.lang.Nullable;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "tutors")
public class Tutor {

    @Id
    private String id;
    private String subject;
    private String userId;
    
    private Double hourlyRate;
    
    @ToString.Exclude
    private List<QualificationFile> qualifications = new ArrayList<>();
    
    @ToString.Exclude
    private Map<String, Availability> availability = new HashMap<>();
    
    private String profileImageUrl;
    
    @ToString.Exclude
    private List<String> lessonType = new ArrayList<>();
    
    private String description;
    
    private String rejectedReason;

    
    private TutorStagedProfileDTO stagedProfile;
    
    private AccountStatus previousStatus;
    
    @ToString.Exclude
    private List<Review> reviews = new ArrayList<>();

    // Return unmodifiable view ONLY for external callers (optional)
    public List<Review> getReviews() {
        return Collections.unmodifiableList(reviews);
    }

    // Internal helper for adding reviews safely
    public void addReview(Review review) {
        if (this.reviews == null) {
            this.reviews = new ArrayList<>();
        }
        this.reviews.add(review);
    }
}
