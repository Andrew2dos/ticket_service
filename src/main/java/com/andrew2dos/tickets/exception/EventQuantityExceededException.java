package com.andrew2dos.tickets.exception;

public class EventQuantityExceededException extends BookingException {
    public EventQuantityExceededException(String message) {
        super(message);
    }
}
