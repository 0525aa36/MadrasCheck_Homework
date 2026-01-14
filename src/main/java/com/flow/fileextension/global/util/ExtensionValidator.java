package com.flow.fileextension.global.util;

import com.flow.fileextension.global.constants.ErrorMessages;
import org.springframework.web.multipart.MultipartFile;

import java.util.regex.Pattern;

/**
 * 확장자 검증을 위한 유틸리티 클래스
 */
public class ExtensionValidator {
    
    private static final Pattern VALID_EXTENSION_PATTERN = Pattern.compile("^[a-zA-Z0-9]+$");
    private static final int MAX_EXTENSION_LENGTH = 20;
    
    /**
     * 확장자 형식 검증 (영문, 숫자만 허용)
     */
    public static boolean isValidFormat(String extension) {
        if (extension == null || extension.trim().isEmpty()) {
            return false;
        }
        return VALID_EXTENSION_PATTERN.matcher(extension).matches();
    }
    
    /**
     * 확장자 길이 검증 (최대 20자)
     */
    public static boolean isValidLength(String extension) {
        return extension != null && extension.length() <= MAX_EXTENSION_LENGTH;
    }
    
    /**
     * 확장자 전체 검증 (형식 + 길이)
     */
    public static void validate(String extension) {
        if (extension == null || extension.trim().isEmpty()) {
            throw new IllegalArgumentException(ErrorMessages.EXTENSION_EMPTY);
        }
        
        if (!isValidLength(extension)) {
            throw new IllegalArgumentException(ErrorMessages.EXTENSION_TOO_LONG);
        }
        
        if (!isValidFormat(extension)) {
            throw new IllegalArgumentException(ErrorMessages.EXTENSION_INVALID_FORMAT);
        }
    }
    
    /**
     * 파일에서 확장자 추출
     */
    public static String extractExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "";
        }
        
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < filename.length() - 1) {
            return filename.substring(lastDotIndex + 1);
        }
        
        return ""; // 확장자 없음
    }
    
    /**
     * 확장자 정규화 (소문자 변환, 공백 제거, 점 제거)
     */
    public static String normalize(String extension) {
        if (extension == null) {
            return "";
        }
        return extension.toLowerCase().replace(".", "").trim();
    }
    
    /**
     * 이중 확장자 검증
     * 예: file.exe.txt → [txt, exe] 둘 다 검사 필요
     */
    public static String[] extractAllExtensions(String filename) {
        if (filename == null || filename.isEmpty()) {
            return new String[0];
        }
        
        String[] parts = filename.split("\\.");
        if (parts.length <= 1) {
            return new String[0];
        }
        
        // 첫 번째 요소는 파일명이므로 제외
        String[] extensions = new String[parts.length - 1];
        System.arraycopy(parts, 1, extensions, 0, extensions.length);
        
        return extensions;
    }
    
    /**
     * MultipartFile에서 확장자 추출 및 정규화
     */
    public static String getExtensionFromFile(MultipartFile file) {
        String filename = file.getOriginalFilename();
        if (filename == null || filename.isEmpty()) {
            throw new IllegalArgumentException(ErrorMessages.FILE_EMPTY);
        }
        
        String extension = extractExtension(filename);
        if (extension.isEmpty()) {
            throw new IllegalArgumentException(ErrorMessages.FILE_NO_EXTENSION);
        }
        
        return normalize(extension);
    }
    
    private ExtensionValidator() {
        throw new AssertionError("유틸리티 클래스는 인스턴스화할 수 없습니다");
    }
}
