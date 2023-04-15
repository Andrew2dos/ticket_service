package com.andrew2dos.tickets.controller;

import com.andrew2dos.tickets.entity.Event;
import com.andrew2dos.tickets.service.EventService;
import com.andrew2dos.tickets.service.TicketService;
import lombok.Data;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@Data
public class EventController {

    public final EventService service;


    @PostMapping("/new_event")
    public ResponseEntity<Event> createEvent(@RequestBody Event event) {
        Event createdEvent = service.createEvent(event);
        if (createdEvent == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(createdEvent, HttpStatus.CREATED);
    }

    @GetMapping("/events")
    public ResponseEntity<List<Event>> getAllEvents() {
        return ResponseEntity.ok(service.getAllEvents());
    }

    @GetMapping("/events/{id}")
    public ResponseEntity<Event>  getEvent(@PathVariable Long id){
        Event event = service.getEvent(id);
        if (event == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(event, HttpStatus.OK);
    }

}
