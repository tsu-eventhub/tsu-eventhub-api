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

    public void approveRequest(User currentUser, UUID targetUserId) {
        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Target user not found"));

        ApprovalRequest request = approvalRequestRepository.findByUser(targetUser)
                .orElseThrow(() -> new ResourceNotFoundException("Approval request not found for user"));

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
}
