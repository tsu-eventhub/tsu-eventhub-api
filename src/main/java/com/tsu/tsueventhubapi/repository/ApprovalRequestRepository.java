package com.tsu.tsueventhubapi.repository;

import com.tsu.tsueventhubapi.model.ApprovalRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ApprovalRequestRepository extends JpaRepository<ApprovalRequest, UUID> {
}
