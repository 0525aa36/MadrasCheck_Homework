package com.flow.fileextension.domain.fixed.service;

import com.flow.fileextension.domain.fixed.dto.FixedExtensionResponseDto;
import com.flow.fileextension.domain.fixed.entity.FixedExtension;
import com.flow.fileextension.domain.fixed.repository.FixedExtensionRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j; // Import Slf4j
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j // Add Slf4j annotation
public class FixedExtensionService {

    private final FixedExtensionRepository fixedExtensionRepository;

    @PostConstruct
    public void initializeDefaultExtensions() {
        log.info("Initializing default fixed extensions..."); // Log start
        List<String> defaults = Arrays.asList("bat", "cmd", "com", "cpl", "exe", "scr", "js", "sh");
        for (String ext : defaults) {
            // Try to find existing extension, ignoring case
            fixedExtensionRepository.findByExtensionIgnoreCase(ext)
                .ifPresentOrElse(
                    // If present, do nothing (preserve its status)
                    foundExt -> log.info("Extension '{}' already exists (id: {}, isBlocked: {}), skipping initialization.", foundExt.getExtension(), foundExt.getId(), foundExt.isBlocked()),
                    // If not present, save a new one
                    () -> {
                        fixedExtensionRepository.save(FixedExtension.builder()
                                .extension(ext)
                                .isBlocked(false)
                                .build());
                        log.info("Saved new default extension: '{}'", ext);
                    }
                );
        }
        log.info("Default fixed extensions initialization complete."); // Log end
    }

    @Transactional(readOnly = true)
    public List<FixedExtensionResponseDto> getAllFixedExtensions() {
        List<FixedExtensionResponseDto> extensions = fixedExtensionRepository.findAll().stream()
                .map(FixedExtensionResponseDto::from)
                .collect(Collectors.toList());
        log.info("Retrieved fixed extensions: {}", extensions);
        return extensions;
    }

    public FixedExtensionResponseDto updateBlockStatus(Long id, Boolean isBlocked) {
        FixedExtension extension = findFixedExtensionById(id);
        log.info("Updating extension {} from isBlocked={} to isBlocked={}", extension.getExtension(), extension.isBlocked(), isBlocked);
        extension.updateBlockStatus(isBlocked);
        FixedExtension savedExtension = fixedExtensionRepository.save(extension);
        log.info("Saved extension {} with isBlocked={}", savedExtension.getExtension(), savedExtension.isBlocked());
        return FixedExtensionResponseDto.from(savedExtension);
    }

    private FixedExtension findFixedExtensionById(Long id) {
        return fixedExtensionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("확장자를 찾을 수 없습니다: " + id));
    }
}
