package com.andrew2dos.tickets.controller;

import com.andrew2dos.tickets.entity.Booking;
import com.andrew2dos.tickets.service.TicketService;
import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Log4j2
@Data
@RestController
public class BookingController {

    private final TicketService service;

    @GetMapping("/all_bookings")
    public ResponseEntity<List<Booking>> getAllBookings() {
        List<Booking> allBookings = service.getAllBookings();
        if (allBookings == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(allBookings, HttpStatus.OK);
    }

    @GetMapping("/bookings/{id}")
    public ResponseEntity<Booking> getBooking(@PathVariable Long id) {
        Booking booking = service.getBooking(id);
        if (booking == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(booking, HttpStatus.OK);
    }

}
