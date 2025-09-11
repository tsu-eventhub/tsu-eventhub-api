package com.tsu.tsueventhubapi.service;

import com.tsu.tsueventhubapi.dto.*;
import com.tsu.tsueventhubapi.exception.ResourceNotFoundException;
import com.tsu.tsueventhubapi.model.Company;
import com.tsu.tsueventhubapi.model.Event;
import com.tsu.tsueventhubapi.model.User;
import com.tsu.tsueventhubapi.repository.EventRepository;
import com.tsu.tsueventhubapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    public List<EventResponseSummary> getAllEvents(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<Event> events;
        
        if (user.getRole().toString().contains("MANAGER")) {
            Company company = user.getCompany();
            if (company == null) {
                throw new IllegalStateException("Manager is not assigned to any company");
            }
            events = eventRepository.findByCompanyId(company.getId());
        } else {
            events = eventRepository.findAll();
        }

        return events.stream()
                .map(this::toSummaryResponse)
                .toList();
    }

    public EventResponseFull createEvent(CreateEventRequest request, UUID managerId) {
        User manager = userRepository.findById(managerId)
                .orElseThrow(() -> new ResourceNotFoundException("Manager not found"));
        
        Company company = manager.getCompany();
        if (company == null) {
            throw new IllegalStateException("Manager is not assigned to any company");
        }

        Event event = Event.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .location(request.getLocation())
                .registrationDeadline(request.getRegistrationDeadline())
                .manager(manager)
                .company(company)
                .build();

        validateEventTimes(event);

        Event saved = eventRepository.save(event);

        return toResponse(saved);
    }

    public EventResponseFull getEventById(UUID eventId, UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));
        
        if (user.getRole().toString().contains("MANAGER")) {
            Company company = user.getCompany();
            if (company == null || !company.getId().equals(event.getCompany().getId())) {
                throw new IllegalStateException("Manager can only view events from their own company");
            }
        }

        return toFullResponse(event);
    }

    public EventResponseFull updateEvent(UUID eventId, UUID managerId, UpdateEventRequest request) {
        User manager = userRepository.findById(managerId)
                .orElseThrow(() -> new ResourceNotFoundException("Manager not found"));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));
        
        if (event.getCompany() == null || !event.getCompany().getId().equals(manager.getCompany().getId())) {
            throw new IllegalStateException("Manager can only update events of their own company");
        }
        
        if (request.getTitle() != null) {
            event.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            event.setDescription(request.getDescription());
        }
        if (request.getStartTime() != null) {
            event.setStartTime(request.getStartTime());
        }
        if (request.getEndTime() != null) {
            event.setEndTime(request.getEndTime());
        }
        if (request.getLocation() != null) {
            event.setLocation(request.getLocation());
        }
        if (request.getRegistrationDeadline() != null) {
            event.setRegistrationDeadline(request.getRegistrationDeadline());
        }

        validateEventTimes(event);

        Event updated = eventRepository.save(event);
        return toFullResponse(updated);
    }

    public void deleteEvent(UUID eventId, UUID managerId) {
        User manager = userRepository.findById(managerId)
                .orElseThrow(() -> new ResourceNotFoundException("Manager not found"));
        
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));
        
        if (!event.getCompany().getId().equals(manager.getCompany().getId())) {
            throw new SecurityException("You cannot delete events from another company");
        }

        eventRepository.delete(event);
    }

    private void validateEventTimes(Event event) {
        Instant startTime = event.getStartTime();
        Instant endTime = event.getEndTime();
        Instant registrationDeadline = event.getRegistrationDeadline();

        if (endTime != null && startTime != null && endTime.isBefore(startTime)) {
            throw new IllegalArgumentException("End time cannot be before start time");
        }

        if (registrationDeadline != null && startTime != null && registrationDeadline.isAfter(startTime)) {
            throw new IllegalArgumentException("Registration deadline cannot be after event start time");
        }
    }

    private EventResponseSummary toSummaryResponse(Event event) {
        return EventResponseSummary.builder()
                .id(event.getId())
                .title(event.getTitle())
                .startTime(event.getStartTime())
                .location(event.getLocation())
                .company(event.getCompany() != null
                        ? new CompanyResponse(event.getCompany().getId(), event.getCompany().getName())
                        : null)
                .build();
    }
    
    private EventResponseFull toFullResponse(Event event) {
        return EventResponseFull.builder()
                .id(event.getId())
                .title(event.getTitle())
                .description(event.getDescription())
                .startTime(event.getStartTime())
                .endTime(event.getEndTime())
                .location(event.getLocation())
                .registrationDeadline(event.getRegistrationDeadline())
                .company(event.getCompany() != null
                        ? new CompanyResponse(event.getCompany().getId(), event.getCompany().getName())
                        : null)
                .build();
    }

    private EventResponseFull toResponse(Event event) {
        return EventResponseFull.builder()
                .id(event.getId())
                .title(event.getTitle())
                .description(event.getDescription())
                .startTime(event.getStartTime())
                .endTime(event.getEndTime())
                .location(event.getLocation())
                .registrationDeadline(event.getRegistrationDeadline())
                .company(event.getCompany() != null
                        ? new CompanyResponse(event.getCompany().getId(), event.getCompany().getName())
                        : null)
                .build();
    }

}
