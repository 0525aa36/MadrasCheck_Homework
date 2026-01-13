package com.flow.fileextension.domain.fixed.service;

import com.flow.fileextension.domain.fixed.entity.FixedExtension;
import com.flow.fileextension.domain.fixed.repository.FixedExtensionRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

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
}
