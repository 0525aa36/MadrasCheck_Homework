package com.flow.fileextension.domain.custom.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomExtensionRequestDto {
    @NotBlank(message = "확장자는 필수입니다")
    @Size(max = 20, message = "확장자는 최대 20자까지 가능합니다")
    @Pattern(regexp = "^[a-zA-Z0-9]+$", message = "확장자는 영문과 숫자만 가능합니다")
    private String extension;
}
