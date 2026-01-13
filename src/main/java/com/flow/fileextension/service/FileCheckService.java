package com.flow.fileextension.service;

import com.flow.fileextension.domain.custom.repository.CustomExtensionRepository;
import com.flow.fileextension.domain.fixed.repository.FixedExtensionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class FileCheckService {

    private final FixedExtensionRepository fixedExtensionRepository;
    private final CustomExtensionRepository customExtensionRepository;

    public boolean isFileExtensionBlocked(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            return false; // Or throw an exception for invalid file name
        }

        String fileExtension = getFileExtension(originalFilename);
        String normalizedExtension = normalizeExtension(fileExtension);

        // Check if it's a blocked fixed extension
        if (fixedExtensionRepository.findByExtension(normalizedExtension)
                                    .map(fixed -> fixed.getIsBlocked())
                                    .orElse(false)) {
            return true;
        }

        // Check if it's a custom extension
        if (customExtensionRepository.existsByExtension(normalizedExtension)) {
            return true;
        }

        return false;
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
