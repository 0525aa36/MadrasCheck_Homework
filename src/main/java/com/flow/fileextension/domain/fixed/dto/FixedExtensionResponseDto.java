package com.flow.fileextension.domain.fixed.dto;

import com.flow.fileextension.domain.fixed.entity.FixedExtension;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FixedExtensionResponseDto {
    private Long id;
    private String extension;
    private boolean isBlocked;

    public static FixedExtensionResponseDto from(FixedExtension entity) {
        return FixedExtensionResponseDto.builder()
                .id(entity.getId())
                .extension(entity.getExtension())
                .isBlocked(entity.isBlocked())
                .build();
    }
}
