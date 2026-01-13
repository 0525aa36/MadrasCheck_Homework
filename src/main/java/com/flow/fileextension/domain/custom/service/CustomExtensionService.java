package com.flow.fileextension.domain.custom.service;

import com.flow.fileextension.domain.custom.dto.CustomExtensionResponseDto;
import com.flow.fileextension.domain.custom.repository.CustomExtensionRepository;
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

    @Transactional(readOnly = true)
    public List<CustomExtensionResponseDto> getAllCustomExtensions() {
        return customExtensionRepository.findAll().stream()
                .map(CustomExtensionResponseDto::from)
                .collect(Collectors.toList());
    }
}
