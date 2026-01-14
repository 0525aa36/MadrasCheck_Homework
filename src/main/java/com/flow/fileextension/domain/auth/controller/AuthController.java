package com.flow.fileextension.domain.auth.controller;

import com.flow.fileextension.global.response.ApiResponse;
import com.flow.fileextension.global.security.SessionUser;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final HttpSession httpSession;

    /**
     * 현재 로그인한 사용자 정보 조회
     */
    @GetMapping("/user")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCurrentUser() {
        SessionUser sessionUser = (SessionUser) httpSession.getAttribute("user");
        
        if (sessionUser == null) {
            return ResponseEntity.ok(ApiResponse.success(null));
        }
        
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", sessionUser.getId());
        userInfo.put("name", sessionUser.getName());
        userInfo.put("email", sessionUser.getEmail());
        userInfo.put("picture", sessionUser.getPicture());
        
        return ResponseEntity.ok(ApiResponse.success(userInfo));
    }

    /**
     * 로그아웃
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout() {
        httpSession.invalidate();
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
