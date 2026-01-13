package com.flow.fileextension.domain.fixed.service;

import com.flow.fileextension.domain.fixed.dto.FixedExtensionResponseDto;
import com.flow.fileextension.domain.fixed.entity.FixedExtension;
import com.flow.fileextension.domain.fixed.repository.FixedExtensionRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class FixedExtensionService {

    private final FixedExtensionRepository fixedExtensionRepository;

    @PostConstruct
    public void initializeDefaultExtensions() {
        List<String> defaults = Arrays.asList("bat", "cmd", "com", "cpl", "exe", "scr", "js", "sh");
        for (String ext : defaults) {
            if (!fixedExtensionRepository.existsByExtension(ext)) {
                fixedExtensionRepository.save(FixedExtension.builder()
                        .extension(ext)
                        .isBlocked(false)
                        .build());
            }
        }
    }

    @Transactional(readOnly = true)
    public List<FixedExtensionResponseDto> getAllFixedExtensions() {
        return fixedExtensionRepository.findAll().stream()
                .map(FixedExtensionResponseDto::from)
                .collect(Collectors.toList());
    }

    public FixedExtensionResponseDto updateBlockStatus(Long id, Boolean isBlocked) {
        FixedExtension extension = findFixedExtensionById(id);
        extension.updateBlockStatus(isBlocked);
        return FixedExtensionResponseDto.from(fixedExtensionRepository.save(extension));
    }

    private FixedExtension findFixedExtensionById(Long id) {
        return fixedExtensionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("확장자를 찾을 수 없습니다: " + id));
    }
}
