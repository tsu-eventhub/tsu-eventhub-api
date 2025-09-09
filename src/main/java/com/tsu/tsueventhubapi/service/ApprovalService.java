package com.tsu.tsueventhubapi.service;

import com.tsu.tsueventhubapi.model.ApprovalRequest;
import com.tsu.tsueventhubapi.model.User;
import com.tsu.tsueventhubapi.repository.ApprovalRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class ApprovalService {

    private final ApprovalRequestRepository approvalRequestRepository;

    public void createApprovalRequest(User user) {
        ApprovalRequest request = ApprovalRequest.builder()
                .user(user)
                .createdAt(Instant.now())
                .processed(false)
                .build();

        approvalRequestRepository.save(request);
    }
}
