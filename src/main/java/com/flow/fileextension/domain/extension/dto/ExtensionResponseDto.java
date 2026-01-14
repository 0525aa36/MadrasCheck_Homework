package com.flow.fileextension.domain.extension.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.flow.fileextension.domain.extension.entity.Extension;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExtensionResponseDto {
    
    private Long id;
    private String extension;
    
    @JsonProperty("fixed")
    private boolean isFixed;
    
    @JsonProperty("blocked")
    private boolean isBlocked;
    
    private String createdByName;
    private String updatedByName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public static ExtensionResponseDto from(Extension extension) {
        return ExtensionResponseDto.builder()
                .id(extension.getId())
                .extension(extension.getExtension())
                .isFixed(extension.isFixed())
                .isBlocked(extension.isBlocked())
                .createdByName(extension.getCreatedBy() != null ? extension.getCreatedBy().getName() : null)
                .updatedByName(extension.getUpdatedBy() != null ? extension.getUpdatedBy().getName() : null)
                .createdAt(extension.getCreatedAt())
                .updatedAt(extension.getUpdatedAt())
                .build();
    }
}
