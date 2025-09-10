package com.tsu.tsueventhubapi.service;

import com.tsu.tsueventhubapi.dto.CompanyResponse;
import com.tsu.tsueventhubapi.dto.CreateEventRequest;
import com.tsu.tsueventhubapi.dto.EventResponseFull;
import com.tsu.tsueventhubapi.dto.EventResponseSummary;
import com.tsu.tsueventhubapi.exception.ResourceNotFoundException;
import com.tsu.tsueventhubapi.model.Company;
import com.tsu.tsueventhubapi.model.Event;
import com.tsu.tsueventhubapi.model.User;
import com.tsu.tsueventhubapi.repository.EventRepository;
import com.tsu.tsueventhubapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
