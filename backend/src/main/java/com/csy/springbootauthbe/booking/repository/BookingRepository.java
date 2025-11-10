package com.csy.springbootauthbe.booking.repository;

import com.csy.springbootauthbe.booking.entity.Booking;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface BookingRepository extends MongoRepository<Booking, String> {
    List<Booking> findByTutorIdAndDate(String tutorId, String date);
    List<Booking> findByStudentId(String studentId);
    @Query("{ 'tutorId': ?0, 'date': { $gte: ?1, $lte: ?2 } }")
    List<Booking> findBookingsByTutorIdAndDateRange(String tutorId, String startDate, String endDate);
    long countByTutorIdAndStatusAndDateBefore(String tutorId, String status, String date);
    List<Booking> findTop5ByTutorIdAndStatusAndDateBeforeOrderByDateDesc(
            String tutorId, String status, String date);
    List<Booking> findByTutorIdAndStatusInAndDateGreaterThanEqualOrderByDateAsc(String tutorId, List<String> statuses, String todayStr);

    List<Booking> findByStudentIdAndStatusInAndDateBeforeOrderByDateDesc(
            String studentId, List<String> statuses, String date);

    List<Booking> findByStudentIdAndDate(String studentId, String date);



}