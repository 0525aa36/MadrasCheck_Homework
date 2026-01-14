package com.flow.fileextension.domain.custom.controller;

import com.flow.fileextension.domain.custom.dto.CustomExtensionResponseDto;
import com.flow.fileextension.domain.custom.service.CustomExtensionService;
import com.flow.fileextension.global.response.ApiResponse;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @deprecated 이 컨트롤러는 더 이상 사용되지 않습니다.
 * {@link com.flow.fileextension.domain.extension.controller.ExtensionController}를 사용하세요.
 */
@Deprecated
@RestController
@RequestMapping("/api/extensions/custom")
@RequiredArgsConstructor
@Validated // Enable validation for this controller
public class
CustomExtensionController {

    private final CustomExtensionService customExtensionService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<CustomExtensionResponseDto>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(customExtensionService.getAllCustomExtensions()));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CustomExtensionResponseDto>> addExtension(
            @RequestParam @NotBlank(message = "확장자는 필수입니다")
            @Size(max = 20, message = "확장자는 최대 20자까지 가능합니다")
            @Pattern(regexp = "^[a-zA-Z0-9]+$", message = "확장자는 영문과 숫자만 가능합니다")
            String extension) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(customExtensionService.addCustomExtension(extension)));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteExtension(@PathVariable Long id) {
        try {
            customExtensionService.deleteCustomExtension(id);
            return ResponseEntity.ok(ApiResponse.success(null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
}
