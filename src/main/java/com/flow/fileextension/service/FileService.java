package com.flow.fileextension.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * 파일 검증 서비스 (테스트용 래퍼)
 */
@Service
@RequiredArgsConstructor
public class FileService {

    private final FileCheckService fileCheckService;

    /**
     * 파일 확장자가 차단되었는지 확인
     * @param file 검사할 파일
     * @return 차단됨(true), 허용됨(false)
     */
    public boolean checkFileExtension(MultipartFile file) {
        return fileCheckService.isFileExtensionBlocked(file);
    }
}
