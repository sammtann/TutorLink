package com.csy.springbootauthbe.booking.dto;

import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BookingRequest {
    private String tutorId;
    private String tutorName;
    private String studentName;
    private String studentId;
    private String lessonType;
    private String date;   // yyyy-MM-dd
    private String start;  // HH:mm
    private String end;
    private BigDecimal amount;
}

