package com.flow.fileextension.domain.user.controller;

import com.flow.fileextension.global.response.ApiResponse;
import com.flow.fileextension.global.security.SessionUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<SessionUser>> getCurrentUser(@AuthenticationPrincipal SessionUser user) {
        if (user == null) {
            // This case should ideally be handled by Spring Security's authentication entry point
            // if the request is unauthenticated. However, for explicit handling or testing,
            // we can return an unauthorized response.
            // For now, we'll rely on Spring Security to return 401 if not authenticated.
            // If the user object is present, it means they are authenticated.
        }
        return ResponseEntity.ok(ApiResponse.success(user));
    }
}
