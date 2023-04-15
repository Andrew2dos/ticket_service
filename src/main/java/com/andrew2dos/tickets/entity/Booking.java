package com.andrew2dos.tickets.entity;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "bookings", schema = "public", catalog = "tickets")
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "booking_id")
    private Long id;

    @Column(name = "customer")
    private String customerName;

    @Column(name = "booking_time")
    private LocalDateTime bookingTime;

    @ManyToOne
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Column(name = "is_paid")
    private boolean paid;

    public Booking() {

    }
    public Booking(Event event, String customerName, LocalDateTime date) {
        this.event = event;
        this.customerName = customerName;
        this.bookingTime = date;
    }


}
