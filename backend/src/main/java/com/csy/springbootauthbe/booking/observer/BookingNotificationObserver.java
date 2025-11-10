package com.csy.springbootauthbe.booking.observer;

import com.csy.springbootauthbe.booking.entity.Booking;
import com.csy.springbootauthbe.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class BookingNotificationObserver implements BookingObserver {

    private final NotificationService notificationService;

    @Override
    public void handleBookingEvent(BookingEvent event) {
        Booking b = event.booking();

        switch (event.type()) {
            case booking_created -> {
                // Tutor
                notificationService.createNotification(
                        b.getTutorId(),
                        "booking_created",
                        b.getId(),
                        b.getStudentName() + " has requested a new booking for " + b.getLessonType() +
                                " on " + b.getDate() + " from " + b.getStart() + " to " + b.getEnd() + "."
                );
                // Student
                notificationService.createNotification(
                        b.getStudentId(),
                        "booking_created",
                        b.getId(),
                        "Your booking request for " + b.getLessonType() + " with " + b.getTutorName() +
                                " on " + b.getDate() + " from " + b.getStart() + " to " + b.getEnd() +
                                " has been created and is pending tutor approval."
                );
            }
            case booking_accepted -> {
                // Student
                notificationService.createNotification(
                        b.getStudentId(),
                        "booking_accepted",
                        b.getId(),
                        "Your booking for " + b.getLessonType() + " from " + b.getTutorName() + " has been confirmed!"
                );
                notificationService.createNotification(
                        b.getStudentId(),
                        "credit_deducted",
                        b.getId(),
                        "An amount of $" + b.getAmount() + " has been deducted for booking " + b.getTutorName() + "."
                );
                // Tutor
                notificationService.createNotification(
                        b.getTutorId(),
                        "credit_released",
                        b.getId(),
                        "An amount of $" + b.getAmount() + " has been released to your wallet for booking " +
                                b.getLessonType() + " for " + b.getStudentName() + "."
                );
            }
            case booking_cancelled -> {
                // Notify the other user
                String recipientId = event.initiatorUserId().equals(b.getStudentId())
                        ? b.getTutorId()
                        : b.getStudentId();

                notificationService.createNotification(
                        recipientId,
                        "booking_cancelled",
                        b.getId(),
                        "Booking for " + b.getLessonType() + " has been cancelled."
                );

                // Notify student about refund
                notificationService.createNotification(
                        b.getStudentId(),
                        "credit_refunded",
                        b.getId(),
                        "An amount of $" + b.getAmount() + " has been refunded for booking " + b.getTutorName() + "."
                );
            }
            case reschedule_requested ->
                notificationService.createNotification(
                        b.getTutorId(),
                        "reschedule_requested",
                        b.getId(),
                        "Student requested reschedule for booking: " + b.getLessonType()
                );
            case reschedule_approved -> {
                notificationService.createNotification(
                        b.getStudentId(),
                        "reschedule_approved",
                        b.getId(),
                        "Your rescheduled booking has been confirmed!"
                );

                // Notify tutor
                notificationService.createNotification(
                        b.getTutorId(),
                        "reschedule_approved",
                        b.getId(),
                        "You confirmed the rescheduled booking."
                );
            }
            // Add more event types later, e.g., CANCELLED
        }
    }
}

