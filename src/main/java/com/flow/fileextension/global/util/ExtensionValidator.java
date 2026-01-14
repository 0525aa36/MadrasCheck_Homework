package com.flow.fileextension.global.util;

import com.flow.fileextension.global.constants.ErrorMessages;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;
import java.util.regex.Pattern;

/**
 * 확장자 검증을 위한 유틸리티 클래스
 */
public class ExtensionValidator {
    
    private static final Pattern VALID_EXTENSION_PATTERN = Pattern.compile("^[a-zA-Z0-9]+$");
    private static final int MAX_EXTENSION_LENGTH = 20;
    
    /**
     * 확장자 형식 검증 (테스트용)
     */
    public static void validateFormat(String extension) {
        if (extension == null || extension.trim().isEmpty()) {
            throw new IllegalArgumentException("확장자는 비어있을 수 없습니다.");
        }
        
        if (extension.length() > MAX_EXTENSION_LENGTH) {
            throw new IllegalArgumentException("확장자는 최대 20자까지 입력 가능합니다.");
        }
        
        if (!VALID_EXTENSION_PATTERN.matcher(extension).matches()) {
            throw new IllegalArgumentException("확장자는 영문자와 숫자만 입력 가능합니다.");
        }
    }
    
    /**
     * 확장자 정규화 (소문자 변환, 점 제거)
     */
    public static String normalizeExtension(String extension) {
        if (extension == null) {
            return "";
        }
        return extension.toLowerCase().replace(".", "").trim();
    }
    
    /**
     * 파일명에 차단된 확장자가 포함되어 있는지 검사 (이중 확장자 포함)
     */
    public static boolean hasBlockedExtension(String filename, Set<String> blockedExtensions) {
        if (filename == null || filename.isEmpty() || blockedExtensions == null || blockedExtensions.isEmpty()) {
            return false;
        }
        
        // 파일명을 점으로 분리
        String[] parts = filename.toLowerCase().split("\\.");
        
        // 첫 번째 요소(파일명)를 제외하고 모든 확장자 검사
        for (int i = 1; i < parts.length; i++) {
            if (blockedExtensions.contains(parts[i])) {
                return true;
            }
        }
        
        return false;
    }
    
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
     * 확장자 형식 검증 - 예외 던지는 버전 (테스트용)
     */
    public static void validateFormat(String extension) {
        if (extension == null || extension.trim().isEmpty()) {
            throw new IllegalArgumentException("확장자는 비어있을 수 없습니다.");
        }
        
        if (extension.length() > MAX_EXTENSION_LENGTH) {
            throw new IllegalArgumentException("확장자는 최대 20자까지 입력 가능합니다.");
        }
        
        if (!VALID_EXTENSION_PATTERN.matcher(extension).matches()) {
            throw new IllegalArgumentException("확장자는 영문자와 숫자만 입력 가능합니다.");
        }
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
     * 확장자 정규화 - 별칭 메서드 (테스트용)
     */
    public static String normalizeExtension(String extension) {
        return normalize(extension);
    }
    
    /**
     * 파일명에 차단된 확장자가 포함되어 있는지 확인 (이중 확장자 포함)
     * @param filename 검사할 파일명
     * @param blockedExtensions 차단된 확장자 Set
     * @return 차단됨(true), 허용됨(false)
     */
    public static boolean hasBlockedExtension(String filename, java.util.Set<String> blockedExtensions) {
        if (filename == null || filename.isEmpty() || blockedExtensions == null || blockedExtensions.isEmpty()) {
            return false;
        }
        
        // 모든 확장자 추출 (이중 확장자 대응)
        String[] parts = filename.toLowerCase().split("\\.");
        
        // 점으로 시작하는 경우 (숨김 파일)
        if (parts.length == 0 || (parts.length == 1 && filename.startsWith("."))) {
            // .exe 같은 경우 처리
            if (filename.startsWith(".") && filename.length() > 1) {
                String ext = filename.substring(1).toLowerCase();
                return blockedExtensions.contains(ext);
            }
            return false;
        }
        
        // 첫 번째 요소가 빈 문자열이면 (점으로 시작) 제외
        int startIndex = (parts[0].isEmpty()) ? 1 : 1; // 첫 번째는 파일명이므로 제외
        
        // 모든 확장자 검사
        for (int i = startIndex; i < parts.length; i++) {
            if (blockedExtensions.contains(parts[i])) {
                return true;
            }
        }
        
        return false;
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
