package com.tsu.tsueventhubapi.repository;

import com.tsu.tsueventhubapi.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface EventRepository extends JpaRepository<Event, UUID> {
}
