package com.flow.fileextension.domain.extension.service;

import com.flow.fileextension.domain.extension.dto.ExtensionResponseDto;
import com.flow.fileextension.domain.extension.entity.Extension;
import com.flow.fileextension.domain.extension.repository.ExtensionRepository;
import com.flow.fileextension.domain.user.entity.User;
import com.flow.fileextension.global.constants.ErrorMessages;
import com.flow.fileextension.global.util.ExtensionValidator;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ExtensionService {

    private final ExtensionRepository extensionRepository;
    
    private static final int MAX_CUSTOM_EXTENSIONS = 200;
    private static final List<String> DEFAULT_FIXED_EXTENSIONS = 
            Arrays.asList("bat", "cmd", "com", "cpl", "exe", "scr", "js", "sh");

    @PostConstruct
    public void initializeDefaultExtensions() {
        log.info("고정 확장자 초기화 시작...");
        
        for (String ext : DEFAULT_FIXED_EXTENSIONS) {
            extensionRepository.findByExtension(ext)
                    .ifPresentOrElse(
                            existingExt -> log.info("확장자 '{}' 이미 존재 (id: {}, isBlocked: {}), 초기화 생략", 
                                    existingExt.getExtension(), existingExt.getId(), existingExt.isBlocked()),
                            () -> {
                                Extension newExt = Extension.builder()
                                        .extension(ext)
                                        .isFixed(true)
                                        .isBlocked(false)
                                        .createdBy(null)  // 시스템 초기화
                                        .build();
                                extensionRepository.save(newExt);
                                log.info("새로운 고정 확장자 저장: '{}'", ext);
                            }
                    );
        }
        
        log.info("고정 확장자 초기화 완료");
    }

    @Transactional(readOnly = true)
    public List<ExtensionResponseDto> getAllFixedExtensions() {
        return extensionRepository.findByIsFixedTrue().stream()
                .map(ExtensionResponseDto::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ExtensionResponseDto> getAllCustomExtensions() {
        return extensionRepository.findByIsFixedFalse().stream()
                .map(ExtensionResponseDto::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ExtensionResponseDto> getAllBlockedExtensions() {
        return extensionRepository.findByIsBlockedTrue().stream()
                .map(ExtensionResponseDto::from)
                .collect(Collectors.toList());
    }

    public ExtensionResponseDto updateBlockStatus(Long id, Boolean isBlocked, User user) {
        Extension extension = findExtensionById(id);
        
        if (!extension.isFixed()) {
            throw new IllegalArgumentException(ErrorMessages.EXTENSION_FIXED_ONLY);
        }
        
        log.info("확장자 {} 차단 상태 변경: {} -> {} (수정자: {})", 
                extension.getExtension(), extension.isBlocked(), isBlocked, 
                user != null ? user.getName() : "시스템");
        
        extension.updateBlockStatus(isBlocked, user);
        Extension savedExtension = extensionRepository.save(extension);
        
        return ExtensionResponseDto.from(savedExtension);
    }

    public ExtensionResponseDto addCustomExtension(String extensionName, User user) {
        // 유틸리티 클래스를 사용한 검증
        ExtensionValidator.validate(extensionName);
        String normalized = ExtensionValidator.normalize(extensionName);
        
        validateMaxCustomCount();
        validateDuplicate(normalized);
        
        Extension newExtension = Extension.builder()
                .extension(normalized)
                .isFixed(false)
                .isBlocked(true)  // 커스텀 확장자는 추가 시 자동으로 차단됨
                .createdBy(user)
                .build();
        
        Extension saved = extensionRepository.save(newExtension);
        log.info("커스텀 확장자 추가: {} (생성자: {})", normalized, user != null ? user.getName() : "알 수 없음");
        
        return ExtensionResponseDto.from(saved);
    }

    public void deleteCustomExtension(Long id, User user) {
        Extension extension = findExtensionById(id);
        
        if (extension.isFixed()) {
            throw new IllegalArgumentException(ErrorMessages.EXTENSION_FIXED_DELETE);
        }
        
        log.info("커스텀 확장자 삭제: {} (삭제자: {})", 
                extension.getExtension(), user != null ? user.getName() : "알 수 없음");
        
        extensionRepository.deleteById(id);
    }

    private Extension findExtensionById(Long id) {
        return extensionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(ErrorMessages.EXTENSION_NOT_FOUND + ": " + id));
    }

    private void validateMaxCustomCount() {
        long customCount = extensionRepository.countByIsFixedFalse();
        if (customCount >= MAX_CUSTOM_EXTENSIONS) {
            throw new IllegalStateException(ErrorMessages.EXTENSION_MAX_COUNT);
        }
    }

    private void validateDuplicate(String extension) {
        if (extensionRepository.existsByExtension(extension)) {
            throw new IllegalArgumentException(ErrorMessages.EXTENSION_DUPLICATE + ": " + extension);
        }
    }
}
