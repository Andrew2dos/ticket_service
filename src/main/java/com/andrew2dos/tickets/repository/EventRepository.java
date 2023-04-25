package com.andrew2dos.tickets.repository;

import com.andrew2dos.tickets.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventRepository extends JpaRepository<Event, Long> {

}
