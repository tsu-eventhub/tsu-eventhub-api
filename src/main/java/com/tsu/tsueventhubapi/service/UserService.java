package com.tsu.tsueventhubapi.service;

import com.tsu.tsueventhubapi.dto.PendingUserResponse;
import com.tsu.tsueventhubapi.exception.ForbiddenException;
import com.tsu.tsueventhubapi.model.ApprovalRequest;
import com.tsu.tsueventhubapi.model.User;
import com.tsu.tsueventhubapi.repository.ApprovalRequestRepository;
import com.tsu.tsueventhubapi.repository.UserRepository;
import com.tsu.tsueventhubapi.security.UserDetailsImpl;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final ApprovalRequestRepository approvalRequestRepository;

    public UserService(UserRepository userRepository, ApprovalRequestRepository approvalRequestRepository) {
        this.userRepository = userRepository;
        this.approvalRequestRepository = approvalRequestRepository;
    }

    public List<PendingUserResponse> getPendingUsers() {
        List<ApprovalRequest> requests;

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();
        User currentUser = userRepository.findById(userDetails.getId()).orElseThrow();


        if (currentUser.getRole().name().equals("DEAN")) {
            requests = approvalRequestRepository.findByProcessedFalse();
        } else if (currentUser.getRole().name().equals("MANAGER")) {
            requests = approvalRequestRepository.findByProcessedFalseAndUser_Company(currentUser.getCompany());
        } else {
            throw new ForbiddenException("Access Denied");
        }

        return requests.stream()
                .map(r -> new PendingUserResponse(
                        r.getUser().getId(),
                        r.getUser().getName(),
                        r.getUser().getEmail(),
                        r.getUser().getRole(),
                        r.getUser().getTelegramUsername(),
                        r.getUser().getCompany() != null ? r.getUser().getCompany().getName() : null
                ))
                .collect(Collectors.toList());
    }
}
