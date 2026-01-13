package com.flow.fileextension.domain.custom.dto;

import com.flow.fileextension.domain.custom.entity.CustomExtension;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class CustomExtensionResponseDto {
    private Long id;
    private String extension;
    private LocalDateTime createdAt;

    public static CustomExtensionResponseDto from(CustomExtension entity) {
        return CustomExtensionResponseDto.builder()
                .id(entity.getId())
                .extension(entity.getExtension())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
