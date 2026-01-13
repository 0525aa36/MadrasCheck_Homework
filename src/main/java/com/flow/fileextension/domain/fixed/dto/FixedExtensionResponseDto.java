package com.flow.fileextension.domain.fixed.dto;

import com.flow.fileextension.domain.fixed.entity.FixedExtension;
import lombok.Builder;
import lombok.Getter; // Add this import
import com.fasterxml.jackson.annotation.JsonProperty;

@Builder
@Getter // Add this annotation
public class FixedExtensionResponseDto {
    private Long id;
    private String extension;
    private boolean isBlocked;

    // Custom getter to ensure correct JSON property name
    @JsonProperty("isBlocked")
    public boolean getIsBlocked() {
        return isBlocked;
    }

    public static FixedExtensionResponseDto from(FixedExtension entity) {
        return FixedExtensionResponseDto.builder()
                .id(entity.getId())
                .extension(entity.getExtension())
                .isBlocked(entity.isBlocked())
                .build();
    }
}
