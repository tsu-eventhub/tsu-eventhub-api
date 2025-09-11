package com.tsu.tsueventhubapi.repository;

import com.tsu.tsueventhubapi.model.ApprovalRequest;
import com.tsu.tsueventhubapi.model.Company;
import com.tsu.tsueventhubapi.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ApprovalRequestRepository extends JpaRepository<ApprovalRequest, UUID> {
    List<ApprovalRequest> findByProcessedFalse();

    List<ApprovalRequest> findByProcessedFalseAndUser_Company(Company company);

    Optional<ApprovalRequest> findByUser(User user);
}
