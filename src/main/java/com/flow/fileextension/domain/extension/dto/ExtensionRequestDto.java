package com.flow.fileextension.domain.extension.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ExtensionRequestDto {
    
    @NotBlank(message = "확장자는 필수입니다")
    @Size(max = 20, message = "확장자는 최대 20자까지 입력 가능합니다")
    private String extension;
}
