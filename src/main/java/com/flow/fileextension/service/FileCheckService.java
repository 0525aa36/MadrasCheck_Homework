package com.flow.fileextension.service;

import com.flow.fileextension.domain.extension.entity.Extension;
import com.flow.fileextension.domain.extension.repository.ExtensionRepository;
import com.flow.fileextension.global.util.ExtensionValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileCheckService {

    private final ExtensionRepository extensionRepository;

    /**
     * 파일 확장자 차단 여부 확인
     * 이중 확장자도 검증 (예: file.exe.txt → exe, txt 둘 다 검사)
     */
    public boolean isFileExtensionBlocked(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            log.warn("파일명이 비어있습니다");
            return false;
        }

        // 모든 확장자 추출 (이중 확장자 대응)
        String[] allExtensions = ExtensionValidator.extractAllExtensions(originalFilename);
        
        if (allExtensions.length == 0) {
            log.info("확장자가 없는 파일: {}", originalFilename);
            return false;
        }
        
        // 모든 확장자 검사
        for (String ext : allExtensions) {
            String normalizedExtension = ExtensionValidator.normalize(ext);
            
            boolean isBlocked = extensionRepository.findByExtension(normalizedExtension)
                    .map(Extension::isBlocked)
                    .orElse(false);
            
            if (isBlocked) {
                log.warn("차단된 확장자 감지: {} (파일: {})", normalizedExtension, originalFilename);
                return true;
            }
        }
        
        log.info("허용된 파일: {} (확장자: {})", originalFilename, String.join(", ", allExtensions));
        return false;
    }
}
