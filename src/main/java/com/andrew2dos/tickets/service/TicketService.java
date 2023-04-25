package com.andrew2dos.tickets.service;

import com.andrew2dos.tickets.dto.PaymentDto;
import com.andrew2dos.tickets.dto.ReserveDto;
import com.andrew2dos.tickets.entity.Event;
import com.andrew2dos.tickets.entity.Booking;
import com.andrew2dos.tickets.repository.EventRepository;
import com.andrew2dos.tickets.repository.BookingRepository;
import com.andrew2dos.tickets.repository.ReserveRepository;
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

    private static final int RESERVE_TIME_SECONDS = 1 * 30;
    private PaymentDto paymentDto;
    private final BookingRepository bookingRepository;
    private final EventRepository eventRepository;
    private final ReserveRepository reserveRepository;

    public void fulfillPayment(PaymentDto payment) {
        if (paymentDto.isPaid()) {
            throw new IllegalStateException("Reserve already paid");
        }

        // Simulate payment processing time
        try {
            Thread.sleep(3000); // Sleep for 3 seconds to simulate payment processing
        } catch (InterruptedException e) {
            // Thread interrupted, handle as needed
            throw new IllegalStateException("Thread interrupted while processing payment", e);
        }

        paymentDto = payment;
        createBooking(paymentDto.getReserveId());

    }

    public void createBooking(Long reserveId){
        ReserveDto reserve = reserveRepository.findById(reserveId)
                .orElseThrow(() -> new IllegalArgumentException("Reserve not found with id: " + reserveId));

        Booking booking;
        for (int i = 0; i < reserve.getReservedQuantity(); i++) {
            booking = new Booking();
            booking.setCustomerName(reserve.getCustomerName());
            booking.setBookingTime(LocalDateTime.now());
            booking.setEvent(eventRepository.findById(reserve.getEvent().getId()).orElseThrow(() -> new RuntimeException("Event not found")));
            bookingRepository.save(booking);
        }

        // Send booking ID to email
//        String customerEmail = booking.getEmail();
//        String subject = "Booking Confirmation";
//        String message = null;
//        if(reserve.getReservedQuantity() == 1) {
//            message = "Thank you for your booking! Your booking ID is: " + booking.getId();
//        } else {
//            message = "Thank you for your booking! Your booking ID is: " + booking.getId();
//        }
//        emailService.sendEmail(customerEmail, subject, message);

        reserveRepository.delete(reserve);
    }

    public void cancelReserve(Long reserveId) {
        ReserveDto reserveDto = reserveRepository.findById(reserveId)
                .orElseThrow(() -> new IllegalArgumentException("In the Cansel method, Reserve not found with id: " + reserveId));
        Event event = eventRepository.findById(reserveDto.getEvent().getId())
                .orElseThrow(() -> new IllegalArgumentException("In the Cansel method, Event not found with id: " + reserveDto.getEvent().getId()));

        if (paymentDto.isPaid()) {
            throw new IllegalStateException("Booking already paid: " + reserveId);
        }

        event.setQuantity(event.getQuantity() + 1); // Increase event quantity since booking is cancelled
        eventRepository.save(event);

        reserveRepository.delete(reserveDto);
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

    public void bookTicket(ReserveDto reserve) {
        Event event = eventRepository.findById(reserve.getEvent().getId()).orElseThrow(() -> new RuntimeException("Event not found"));

        paymentDto = new PaymentDto();
        paymentDto.setReserveId(0L);
        paymentDto.setPaid(false);

        if (event.getQuantity() <= 0) {
            throw new IllegalStateException("No tickets available for event: " + event.getEventName());
        } else if (event.getQuantity() < reserve.getReservedQuantity()) {
            throw new IllegalStateException("Not enough tickets. Only " + event.getQuantity() + " are available");
        }

        ReentrantLock eventLock = eventLocks.computeIfAbsent(reserve.getEvent().getId(), id -> new ReentrantLock());
        try {
            if (eventLock.tryLock(RESERVE_TIME_SECONDS, TimeUnit.SECONDS)) {
                try {
                    // Check quantity again, in case another thread booked the last ticket while waiting for the lock
                    event = eventRepository.findById(reserve.getEvent().getId())
                            .orElseThrow(() -> new IllegalArgumentException("Event not found with id: " + reserve.getEvent().getId()));

                    if (event.getQuantity() <= 0) {
                        throw new IllegalStateException("No tickets available for event: " + event.getEventName());
                    } else if (event.getQuantity() < reserve.getReservedQuantity()) {
                        throw new IllegalStateException("Not enough tickets. Only " + event.getQuantity() + " are available");
                    }

                    // Update event quantity and save booking
                    event.setQuantity(event.getQuantity() - reserve.getReservedQuantity());
                    log.info("Quantity after minus - " + event.getQuantity());
                    eventRepository.save(event);

                    reserveRepository.save(reserve);

                    // Start a separate thread to monitor the payment timeout
                    Thread paymentTimeoutThread = new Thread(() -> {
                        try {
                            Thread.sleep(RESERVE_TIME_SECONDS * 1000); // Sleep for 1 minute to wait for payment
                            if (!paymentDto.isPaid()) {
                                // If payment not fulfilled within 1 minute, cancel the booking
                                cancelReserve(reserve.getReserveId());
                                throw new IllegalStateException("Sorry. Time expired. Your reserve with number "
                                        + reserve.getReserveId() + " has canceled" );
                            }
                        } catch (InterruptedException e) {
                            // Thread interrupted, handle as needed
                            throw new IllegalStateException("Thread interrupted while waiting for payment", e);
                        }
                    });
                    paymentTimeoutThread.start();


//                    return reserve;
                } finally {
                    eventLock.unlock();
                    eventLocks.remove(reserve.getEvent().getId());
                }
            } else {
                throw new IllegalStateException("Unable to acquire lock for event: " + event.getEventName());
            }
        } catch (InterruptedException e) {
            // Thread interrupted while waiting for lock, handle as needed
            throw new IllegalStateException("Thread interrupted while waiting for lock", e);
        }
    }

}