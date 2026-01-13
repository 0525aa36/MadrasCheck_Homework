package com.flow.fileextension.controller;

import com.flow.fileextension.global.response.ApiResponse;
import com.flow.fileextension.service.FileCheckService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileCheckController {

    private final FileCheckService fileCheckService;

    @PostMapping("/check")
    public ResponseEntity<ApiResponse<Boolean>> checkFileExtension(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("파일을 선택해주세요."));
        }
        try {
            boolean isBlocked = fileCheckService.isFileExtensionBlocked(file);
            return ResponseEntity.ok(ApiResponse.success(isBlocked));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.error("파일 확장자 확인 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }
}
