package com.tsu.tsueventhubapi.service;

import com.tsu.tsueventhubapi.dto.EventResponse;
import com.tsu.tsueventhubapi.model.Event;
import com.tsu.tsueventhubapi.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;

    public List<EventResponse> getAllEvents() {
        return eventRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    private EventResponse toResponse(Event event) {
        return new EventResponse(
                event.getId(),
                event.getTitle(),
                event.getDescription(),
                event.getStartTime(),
                event.getEndTime(),
                event.getLocation(),
                event.getRegistrationDeadline(),
                event.getCompany() != null ? event.getCompany().getId() : null
        );
    }
}
