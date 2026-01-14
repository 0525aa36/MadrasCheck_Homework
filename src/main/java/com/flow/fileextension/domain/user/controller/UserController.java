package com.flow.fileextension.domain.user.controller;

import com.flow.fileextension.global.response.ApiResponse;
import com.flow.fileextension.global.security.SessionUser;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<Map<String, String>>> getCurrentUser(HttpSession session) {
        SessionUser sessionUser = (SessionUser) session.getAttribute("user");

        if (sessionUser == null) {
            log.warn("세션에 사용자 정보가 없습니다");
            return ResponseEntity.status(401)
                    .body(ApiResponse.error("인증되지 않은 사용자입니다"));
        }

        Map<String, String> userInfo = new HashMap<>();
        userInfo.put("name", sessionUser.getName());
        userInfo.put("email", sessionUser.getEmail());
        userInfo.put("picture", sessionUser.getPicture());

        log.info("현재 사용자 정보 반환: {}", sessionUser.getName());

        return ResponseEntity.ok(ApiResponse.success(userInfo));
    }
}
