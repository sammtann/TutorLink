package com.csy.springbootauthbe.booking.observer;

import com.csy.springbootauthbe.booking.entity.Booking;

public record BookingEvent(Booking booking, com.csy.springbootauthbe.booking.observer.BookingEvent.Type type,
                           String initiatorUserId) {
    public enum Type {booking_created, booking_accepted, booking_cancelled, reschedule_requested, reschedule_approved}

}
