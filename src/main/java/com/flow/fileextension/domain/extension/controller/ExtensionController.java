package com.flow.fileextension.domain.extension.controller;

import com.flow.fileextension.domain.extension.dto.ExtensionRequestDto;
import com.flow.fileextension.domain.extension.dto.ExtensionResponseDto;
import com.flow.fileextension.domain.extension.service.ExtensionService;
import com.flow.fileextension.domain.user.entity.User;
import com.flow.fileextension.domain.user.repository.UserRepository;
import com.flow.fileextension.global.response.ApiResponse;
import com.flow.fileextension.global.security.SessionUser;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/extensions")
@RequiredArgsConstructor
@Validated
public class ExtensionController {

    private final ExtensionService extensionService;
    private final UserRepository userRepository;
    private final HttpSession httpSession;

    /**
     * 고정 확장자 목록 조회
     */
    @GetMapping("/fixed")
    public ResponseEntity<ApiResponse<List<ExtensionResponseDto>>> getAllFixedExtensions() {
        return ResponseEntity.ok(ApiResponse.success(extensionService.getAllFixedExtensions()));
    }

    /**
     * 커스텀 확장자 목록 조회
     */
    @GetMapping("/custom")
    public ResponseEntity<ApiResponse<List<ExtensionResponseDto>>> getAllCustomExtensions() {
        return ResponseEntity.ok(ApiResponse.success(extensionService.getAllCustomExtensions()));
    }

    /**
     * 차단된 확장자 목록 조회 (고정 + 커스텀)
     */
    @GetMapping("/blocked")
    public ResponseEntity<ApiResponse<List<ExtensionResponseDto>>> getAllBlockedExtensions() {
        return ResponseEntity.ok(ApiResponse.success(extensionService.getAllBlockedExtensions()));
    }

    /**
     * 고정 확장자 차단 상태 변경
     */
    @PatchMapping("/fixed/{id}/block")
    public ResponseEntity<ApiResponse<ExtensionResponseDto>> updateBlockStatus(
            @PathVariable Long id,
            @RequestParam Boolean isBlocked) {
        try {
            User currentUser = getCurrentUser();
            ExtensionResponseDto response = extensionService.updateBlockStatus(id, isBlocked, currentUser);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 커스텀 확장자 추가
     */
    @PostMapping("/custom")
    public ResponseEntity<ApiResponse<ExtensionResponseDto>> addCustomExtension(
            @RequestParam @NotBlank(message = "확장자는 필수입니다")
            @Size(max = 20, message = "확장자는 최대 20자까지 가능합니다")
            @Pattern(regexp = "^[a-zA-Z0-9]+$", message = "확장자는 영문과 숫자만 가능합니다")
            String extension) {
        try {
            User currentUser = getCurrentUser();
            ExtensionResponseDto response = extensionService.addCustomExtension(extension, currentUser);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(response));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 커스텀 확장자 삭제
     */
    @DeleteMapping("/custom/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCustomExtension(@PathVariable Long id) {
        try {
            User currentUser = getCurrentUser();
            extensionService.deleteCustomExtension(id, currentUser);
            return ResponseEntity.ok(ApiResponse.success(null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 현재 로그인한 사용자 조회
     */
    private User getCurrentUser() {
        SessionUser sessionUser = (SessionUser) httpSession.getAttribute("user");
        if (sessionUser == null) {
            log.warn("세션에 사용자 정보가 없습니다. 비로그인 상태로 처리합니다.");
            return null; // 비로그인 상태
        }
        
        log.info("세션 사용자 정보: email={}, name={}", sessionUser.getEmail(), sessionUser.getName());
        
        return userRepository.findByEmail(sessionUser.getEmail())
                .orElseGet(() -> {
                    log.warn("DB에서 사용자를 찾을 수 없습니다: {}", sessionUser.getEmail());
                    return null;
                });
    }
}
