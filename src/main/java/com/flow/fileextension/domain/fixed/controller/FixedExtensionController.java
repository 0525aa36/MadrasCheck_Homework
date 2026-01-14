package com.flow.fileextension.domain.fixed.controller;

import com.flow.fileextension.global.response.ApiResponse;
import com.flow.fileextension.domain.fixed.dto.FixedExtensionResponseDto;
import com.flow.fileextension.domain.fixed.service.FixedExtensionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @deprecated 이 컨트롤러는 더 이상 사용되지 않습니다.
 * {@link com.flow.fileextension.domain.extension.controller.ExtensionController}를 사용하세요.
 */
@Deprecated
@RestController
@RequestMapping("/api/extensions/fixed")
@RequiredArgsConstructor
public class FixedExtensionController {

    private final FixedExtensionService fixedExtensionService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<FixedExtensionResponseDto>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(fixedExtensionService.getAllFixedExtensions()));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<FixedExtensionResponseDto>> updateStatus(
            @PathVariable Long id,
            @RequestParam Boolean isBlocked) {
        try {
            return ResponseEntity.ok(
                    ApiResponse.success(fixedExtensionService.updateBlockStatus(id, isBlocked))
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
}
