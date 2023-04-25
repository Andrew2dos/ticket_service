package com.andrew2dos.tickets.controller;

import com.andrew2dos.tickets.dto.PaymentDto;
import com.andrew2dos.tickets.dto.ReserveDto;
import com.andrew2dos.tickets.entity.Booking;
import com.andrew2dos.tickets.service.TicketService;
import lombok.Data;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Data
@RestController
public class ReserveController {
    private final TicketService service;


    @PostMapping("/reserve")
    public ResponseEntity<String> reserveTicket(@RequestBody ReserveDto reserve){
        try {
        service.bookTicket(reserve);
            return ResponseEntity.ok("" + reserve);
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        }
    }

    @PostMapping("/paid")
    public ResponseEntity<String> paidBooking(@RequestBody PaymentDto payment) {
        try {
            service.fulfillPayment(payment);
            return ResponseEntity.ok("Successful. The order " + payment.getReserveId() + " paid");
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        }
    }
}
