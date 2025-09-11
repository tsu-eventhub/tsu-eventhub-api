package com.tsu.tsueventhubapi.repository;

import com.tsu.tsueventhubapi.model.Registration;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RegistrationRepository extends JpaRepository<Registration, UUID> {
    Optional<Registration> findByStudentIdAndEventId(UUID studentId, UUID eventId);

    List<Registration> findByStudentIdAndUnregisteredAtIsNull(UUID studentId);
}
