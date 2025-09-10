package com.tsu.tsueventhubapi.service;

import com.tsu.tsueventhubapi.enumeration.Status;
import com.tsu.tsueventhubapi.exception.ForbiddenException;
import com.tsu.tsueventhubapi.exception.ResourceNotFoundException;
import com.tsu.tsueventhubapi.model.ApprovalRequest;
import com.tsu.tsueventhubapi.model.User;
import com.tsu.tsueventhubapi.repository.ApprovalRequestRepository;
import com.tsu.tsueventhubapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ApprovalService {

    private final UserRepository userRepository;
    private final ApprovalRequestRepository approvalRequestRepository;

    public void createApprovalRequest(User user) {
        ApprovalRequest request = ApprovalRequest.builder()
                .user(user)
                .createdAt(Instant.now())
                .processed(false)
                .build();

        approvalRequestRepository.save(request);
    }

    public void approveRequest(User currentUser, UUID requestId) {
        ApprovalRequest request = approvalRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found"));

        if (request.isProcessed()) {
            throw new IllegalStateException("This request has already been processed");
        }

        User targetUser = request.getUser();

        if (currentUser.getId().equals(targetUser.getId())) {
            throw new ForbiddenException("You cannot approve yourself");
        }

        if (currentUser.getRole().name().equals("MANAGER") &&
                currentUser.getStatus().name().equals("PENDING")) {
            throw new ForbiddenException("A manager with PENDING status cannot approve other users");
        }

        request.setProcessed(true);
        approvalRequestRepository.save(request);

        targetUser.setStatus(Status.APPROVED);
        userRepository.save(targetUser);
    }

    public void rejectRequest(User currentUser, UUID requestId, String reason) {
        ApprovalRequest request = approvalRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found"));

        if (request.isProcessed()) {
            throw new IllegalStateException("This request has already been processed");
        }

        User targetUser = request.getUser();

        if (currentUser.getId().equals(targetUser.getId())) {
            throw new ForbiddenException("You cannot reject yourself");
        }

        if (currentUser.getRole().name().equals("MANAGER") &&
                currentUser.getStatus().name().equals("PENDING")) {
            throw new ForbiddenException("A manager with PENDING status cannot reject other users");
        }
        
        request.setProcessed(true);
        request.setRejectionReason(reason);
        approvalRequestRepository.save(request);

        targetUser.setStatus(Status.REJECTED);
        targetUser.setDeletedAt(Instant.now());
        userRepository.save(targetUser);
    }

}
