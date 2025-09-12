package com.tsu.tsueventhubapi.service;

import com.tsu.tsueventhubapi.dto.PendingUserResponse;
import com.tsu.tsueventhubapi.exception.ForbiddenException;
import com.tsu.tsueventhubapi.exception.ResourceNotFoundException;
import com.tsu.tsueventhubapi.exception.UnauthorizedException;
import com.tsu.tsueventhubapi.model.ApprovalRequest;
import com.tsu.tsueventhubapi.model.User;
import com.tsu.tsueventhubapi.repository.ApprovalRequestRepository;
import com.tsu.tsueventhubapi.repository.UserRepository;
import com.tsu.tsueventhubapi.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
    
    public Page<PendingUserResponse> getPendingUsers(int page, int size) {
        User currentUser = getCurrentUser();

        Pageable pageable = PageRequest.of(page, size);
        Page<ApprovalRequest> requests;
        switch (currentUser.getRole()) {
            case DEAN -> requests = approvalRequestRepository.findByProcessedFalseAndUser_DeletedAtIsNull(pageable);
            case MANAGER -> requests = approvalRequestRepository.findByProcessedFalseAndUser_Company(currentUser.getCompany(), pageable);
            default -> throw new ForbiddenException("Access Denied");
        }

        return requests.map(r -> new PendingUserResponse(
                r.getId(),
                r.getUser().getName(),
                r.getUser().getEmail(),
                r.getUser().getRole(),
                r.getUser().getTelegramUsername(),
                r.getUser().getCompany() != null ? r.getUser().getCompany().getName() : null
        ));
    }

    public void approveUser(UUID requestId) {
        User currentUser = getCurrentUser();
        approvalService.approveRequest(currentUser, requestId);
    }

    public void rejectUser(UUID requestId, String reason) {
        User currentUser = getCurrentUser();
        approvalService.rejectRequest(currentUser, requestId, reason);
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof UserDetailsImpl userDetails)) {
            throw new UnauthorizedException("Unauthorized");
        }
        return userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Current user not found"));
    }
}
