package com.csy.springbootauthbe.booking.entity;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "bookings")
public class Booking {

    private String id;
    private String tutorId;
    private String studentId;
    private String tutorName;
    private String studentName;
    private String date;
    private String start;
    private String end;
    private String lessonType;
    private String status;
    private String originalBookingId;

    private BigDecimal amount; // 💰 Total booking cost

    @CreatedDate
    @Field("created_at")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Field("updated_at")
    private LocalDateTime updatedAt;
}
