package com.tsu.tsueventhubapi.util;

import com.tsu.tsueventhubapi.exception.ForbiddenException;
import com.tsu.tsueventhubapi.exception.ResourceNotFoundException;
import com.tsu.tsueventhubapi.exception.UnauthorizedException;
import com.tsu.tsueventhubapi.model.User;
import com.tsu.tsueventhubapi.repository.UserRepository;
import com.tsu.tsueventhubapi.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class ApprovedOnlyAspect {

    private final UserRepository userRepository;

    @Before("@within(approvedOnly) || @annotation(approvedOnly)")
    public void checkApprovedStatus(ApprovedOnly approvedOnly) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof UserDetailsImpl details)) {
            throw new UnauthorizedException("Unauthorized");
        }

        User currentUser = userRepository.findById(details.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!"APPROVED".equals(currentUser.getStatus().name())) {
            throw new ForbiddenException("Only approved users can access this resource");
        }
    }
}
