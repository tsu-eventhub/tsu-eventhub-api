package com.tsu.tsueventhubapi.service;

import com.tsu.tsueventhubapi.dto.CreateEventRequest;
import com.tsu.tsueventhubapi.dto.EventResponse;
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

    public List<EventResponse> getAllEvents(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

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
                .map(this::toResponse)
                .toList();
    }

    public EventResponse createEvent(CreateEventRequest request, UUID managerId) {
        User manager = userRepository.findById(managerId)
                .orElseThrow(() -> new IllegalArgumentException("Manager not found"));
        
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

    private EventResponse toResponse(Event event) {
        return EventResponse.builder()
                .id(event.getId())
                .title(event.getTitle())
                .description(event.getDescription())
                .startTime(event.getStartTime())
                .endTime(event.getEndTime())
                .location(event.getLocation())
                .registrationDeadline(event.getRegistrationDeadline())
                .companyId(event.getCompany() != null ? event.getCompany().getId() : null)
                .build();
    }

}
