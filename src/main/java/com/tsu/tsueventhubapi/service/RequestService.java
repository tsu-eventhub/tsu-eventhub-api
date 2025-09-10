package com.tsu.tsueventhubapi.service;

import com.tsu.tsueventhubapi.dto.PendingUserResponse;
import com.tsu.tsueventhubapi.exception.ForbiddenException;
import com.tsu.tsueventhubapi.exception.ResourceNotFoundException;
import com.tsu.tsueventhubapi.model.ApprovalRequest;
import com.tsu.tsueventhubapi.model.User;
import com.tsu.tsueventhubapi.repository.ApprovalRequestRepository;
import com.tsu.tsueventhubapi.repository.UserRepository;
import com.tsu.tsueventhubapi.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RequestService {

    private final ApprovalService approvalService;
    private final UserRepository userRepository;
    private final ApprovalRequestRepository approvalRequestRepository;
    
    public List<PendingUserResponse> getPendingUsers() {
        List<ApprovalRequest> requests;

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();
        User currentUser = userRepository.findById(userDetails.getId()).orElseThrow();
        
        if (currentUser.getRole().name().equals("DEAN")) {
            requests = approvalRequestRepository.findByProcessedFalse();
        } else if (currentUser.getRole().name().equals("MANAGER")) {
            if (!currentUser.getStatus().name().equals("APPROVED")) {
                throw new ForbiddenException("Pending managers cannot approve users");
            }
            requests = approvalRequestRepository.findByProcessedFalseAndUser_Company(currentUser.getCompany());
        } else {
            throw new ForbiddenException("Access Denied");
        }

        return requests.stream()
                .filter(r -> r.getUser().getDeletedAt() == null)
                .map(r -> new PendingUserResponse(
                        r.getId(),
                        r.getUser().getName(),
                        r.getUser().getEmail(),
                        r.getUser().getRole(),
                        r.getUser().getTelegramUsername(),
                        r.getUser().getCompany() != null ? r.getUser().getCompany().getName() : null
                ))
                .collect(Collectors.toList());
    }

    public void approveUser(UUID requestId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();
        User currentUser = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Current user not found"));
        
        approvalService.approveRequest(currentUser, requestId);
    }

    public void rejectUser(UUID requestId, String reason) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();
        User currentUser = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Current user not found"));

        approvalService.rejectRequest(currentUser, requestId, reason);
    }

}
