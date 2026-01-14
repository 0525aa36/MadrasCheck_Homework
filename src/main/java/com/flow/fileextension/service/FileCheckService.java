package com.flow.fileextension.service;

import com.flow.fileextension.domain.extension.repository.ExtensionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class FileCheckService {

    private final ExtensionRepository extensionRepository;

    public boolean isFileExtensionBlocked(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            return false;
        }

        String fileExtension = getFileExtension(originalFilename);
        String normalizedExtension = normalizeExtension(fileExtension);

        // 차단된 확장자인지 확인 (고정 또는 커스텀)
        return extensionRepository.findByExtension(normalizedExtension)
                .map(extension -> extension.isBlocked())
                .orElse(false);
    }

    private String getFileExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < filename.length() - 1) {
            return filename.substring(dotIndex + 1);
        }
        return ""; // No extension or invalid
    }

    private String normalizeExtension(String extension) {
        return extension.toLowerCase().trim();
    }
}
