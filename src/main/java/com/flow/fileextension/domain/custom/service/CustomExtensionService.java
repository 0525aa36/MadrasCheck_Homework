package com.flow.fileextension.domain.custom.service;

import com.flow.fileextension.domain.custom.dto.CustomExtensionResponseDto;
import com.flow.fileextension.domain.custom.entity.CustomExtension;
import com.flow.fileextension.domain.custom.repository.CustomExtensionRepository;
import com.flow.fileextension.domain.fixed.repository.FixedExtensionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CustomExtensionService {

    private final CustomExtensionRepository customExtensionRepository;
    private final FixedExtensionRepository fixedExtensionRepository; // Inject FixedExtensionRepository

    private static final int MAX_CUSTOM_EXTENSIONS = 200;

    @Transactional(readOnly = true)
    public List<CustomExtensionResponseDto> getAllCustomExtensions() {
        return customExtensionRepository.findAll().stream()
                .map(CustomExtensionResponseDto::from)
                .collect(Collectors.toList());
    }

    public CustomExtensionResponseDto addCustomExtension(String extension) {
        String normalized = normalizeExtension(extension);
        validateMaxCount();
        validateDuplicateInCustom(normalized);
        validateDuplicateInFixed(normalized);

        CustomExtension saved = customExtensionRepository.save(
                CustomExtension.builder()
                        .extension(normalized)
                        .build()
        );
        return CustomExtensionResponseDto.from(saved);
    }

    public void deleteCustomExtension(Long id) {
        validateExtensionExists(id);
        customExtensionRepository.deleteById(id);
    }

    private String normalizeExtension(String extension) {
        return extension.toLowerCase().replace(".", "").trim();
    }

    private void validateMaxCount() {
        if (customExtensionRepository.count() >= MAX_CUSTOM_EXTENSIONS) {
            throw new IllegalStateException("커스텀 확장자는 최대 " + MAX_CUSTOM_EXTENSIONS + "개까지만 추가 가능합니다");
        }
    }

    private void validateDuplicateInCustom(String extension) {
        if (customExtensionRepository.existsByExtension(extension)) {
            throw new IllegalArgumentException("이미 존재하는 확장자입니다: " + extension);
        }
    }

    private void validateDuplicateInFixed(String extension) {
        if (fixedExtensionRepository.existsByExtension(extension)) {
            throw new IllegalArgumentException("고정 확장자에 이미 존재합니다: " + extension);
        }
    }

    private void validateExtensionExists(Long id) {
        if (!customExtensionRepository.existsById(id)) {
            throw new IllegalArgumentException("확장자를 찾을 수 없습니다: " + id);
        }
    }
}
