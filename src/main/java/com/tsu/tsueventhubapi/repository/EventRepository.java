package com.tsu.tsueventhubapi.repository;

import com.tsu.tsueventhubapi.model.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface EventRepository extends JpaRepository<Event, UUID> {
    Page<Event> findByCompanyId(UUID companyId,  Pageable pageable);

    Page<Event> findAll(Pageable pageable);
}
