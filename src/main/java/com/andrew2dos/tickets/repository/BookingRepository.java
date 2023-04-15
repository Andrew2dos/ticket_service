package com.andrew2dos.tickets.repository;

import com.andrew2dos.tickets.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingRepository extends JpaRepository<Booking, Long> {

}
