package com.dcaguard.controller;

import com.dcaguard.dto.response.UserResponse;
import com.dcaguard.entity.User;
import com.dcaguard.security.SecurityUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @PostMapping("/login")
    public ResponseEntity<UserResponse> login() {
        User user = SecurityUtils.getCurrentUser();
        return ResponseEntity.ok(toUserResponse(user));
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> me() {
        User user = SecurityUtils.getCurrentUser();
        return ResponseEntity.ok(toUserResponse(user));
    }

    private UserResponse toUserResponse(User user) {
        UserResponse r = new UserResponse();
        r.setId(user.getId());
        r.setEmail(user.getEmail());
        r.setDisplayName(user.getDisplayName());
        r.setPlanType(user.getPlanType().name());
        r.setHasAds(user.hasAds());
        r.setMaxPortfolios(user.getMaxPortfolios());
        r.setMaxDcaPlans(user.getMaxDcaPlans());
        r.setMaxHistoryDays(user.getMaxHistoryDays());
        return r;
    }
}
