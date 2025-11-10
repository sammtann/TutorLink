package com.csy.springbootauthbe.booking.observer;

public interface BookingObserver {
    void handleBookingEvent(BookingEvent event);
}
