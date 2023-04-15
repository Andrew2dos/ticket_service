package com.andrew2dos.tickets.service;

import com.andrew2dos.tickets.entity.Event;
import com.andrew2dos.tickets.entity.Booking;
import com.andrew2dos.tickets.repository.EventRepository;
import com.andrew2dos.tickets.repository.BookingRepository;
import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

@Service
@Data
@Log4j2
public class TicketService {

    private ConcurrentMap<Long, ReentrantLock> eventLocks = new ConcurrentHashMap<>();

    public static final int RESERVE_TIME_SECONDS = 1 * 60;
    private final BookingRepository bookingRepository;
    private final EventRepository eventRepository;

    public Booking bookTicket(String eventName, String customerName) throws IllegalStateException {
        Event event = eventRepository.findEventByEventName(eventName);
        Long eventId = event.getId();

        if (event.getQuantity() <= 0) {
            throw new IllegalStateException("No tickets available for event: " + event.getEventName());
        }

        ReentrantLock eventLock = eventLocks.computeIfAbsent(eventId, id -> new ReentrantLock());
        try {
            if (eventLock.tryLock(RESERVE_TIME_SECONDS, TimeUnit.SECONDS)) {
                try {
                    // Check quantity again, in case another thread booked the last ticket while waiting for the lock
                    event = eventRepository.findById(eventId)
                            .orElseThrow(() -> new IllegalArgumentException("Event not found with id: " + eventId));

                    if (event.getQuantity() <= 0) {
                        throw new IllegalStateException("No tickets available for event: " + event.getEventName());
                    }

                    // Update event quantity and save booking
                    event.setQuantity(event.getQuantity() - 1);
                    eventRepository.save(event);

                    Booking booking = new Booking();
                    booking.setCustomerName(customerName);
                    booking.setBookingTime(LocalDateTime.now());
                    booking.setEvent(event);
                    booking.setPaid(false); // Payment status will be updated later
                    booking = bookingRepository.save(booking);

                    // Start a separate thread to monitor the payment timeout
                    Booking finalBooking = booking;
                    Thread paymentTimeoutThread = new Thread(() -> {
                        try {
                            Thread.sleep(RESERVE_TIME_SECONDS * 1000); // Sleep for 1 minute to wait for payment
                            if (!finalBooking.isPaid()) {
                                // If payment not fulfilled within 1 minute, cancel the booking
                                cancelBooking(finalBooking.getId());
                            }
                        } catch (InterruptedException e) {
                            // Thread interrupted, handle as needed
                            throw new IllegalStateException("Thread interrupted while waiting for payment", e);
                        }
                    });
                    paymentTimeoutThread.start();

                    return booking;
                } finally {
                    eventLock.unlock();
                    eventLocks.remove(eventId);
                }
            } else {
                throw new IllegalStateException("Unable to acquire lock for event: " + event.getEventName());
            }
        } catch (InterruptedException e) {
            // Thread interrupted while waiting for lock, handle as needed
            throw new IllegalStateException("Thread interrupted while waiting for lock", e);
        }
    }

    public void fulfillPayment(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found with id: " + bookingId));

        if (booking.isPaid()) {
            throw new IllegalStateException("Booking already paid: " + bookingId);
        }

        // Simulate payment processing time
        try {
            Thread.sleep(3000); // Sleep for 3 seconds to simulate payment processing
        } catch (InterruptedException e) {
            // Thread interrupted, handle as needed
            throw new IllegalStateException("Thread interrupted while processing payment", e);
        }

        // Update booking payment status
        booking.setPaid(true);
        bookingRepository.save(booking);
    }

    public void cancelBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found with id: " + bookingId));
        if (booking.isPaid()) {
            throw new IllegalStateException("Booking already paid: " + bookingId);
        }

        Event event = booking.getEvent();
        event.setQuantity(event.getQuantity() + 1); // Increase event quantity since booking is cancelled
        eventRepository.save(event);

        bookingRepository.delete(booking);
    }

    public Booking getBooking(Long bookId) {
        Optional<Booking> optionalBooking = bookingRepository.findById(bookId);
        if (optionalBooking.isPresent()) {
            return optionalBooking.get();
        } else {
            throw new IllegalArgumentException("Booking not found with ID: " + bookId);
        }
    }

    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

}