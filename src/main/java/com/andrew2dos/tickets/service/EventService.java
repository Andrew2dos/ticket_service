package com.andrew2dos.tickets.service;

import com.andrew2dos.tickets.entity.Event;
import com.andrew2dos.tickets.repository.EventRepository;
import lombok.Data;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Data
public class EventService {

    private final EventRepository eventRepository;

    public Event createEvent(Event event) {
        return eventRepository.save(event);
    }

    public Event getEvent(Long eventId) {
        Optional<Event> optionalEvent = eventRepository.findById(eventId);
        if (optionalEvent.isPresent()) {
            return optionalEvent.get();
        } else {
            throw new IllegalArgumentException("Event not found with ID: " + eventId);
        }
    }

    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }

}
