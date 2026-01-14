package com.flow.fileextension.service;

import com.flow.fileextension.domain.extension.service.ExtensionService;
import com.flow.fileextension.global.util.ExtensionValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileService {

    private final ExtensionService extensionService;

    /**
     * 파일 확장자가 차단되었는지 확인
     * @param file 검증할 파일
     * @return true: 차단됨, false: 허용됨
     */
    public boolean checkFileExtension(MultipartFile file) {
        String filename = file.getOriginalFilename();
        if (filename == null || filename.isEmpty()) {
            return false;
        }

        // 차단된 확장자 목록 조회
        List<String> blockedExtensions = extensionService.getBlockedExtensions();
        Set<String> blockedSet = new HashSet<>(blockedExtensions);

        // 이중 확장자 검증
        return ExtensionValidator.hasBlockedExtension(filename, blockedSet);
    }
}
