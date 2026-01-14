package com.flow.fileextension.global.constants;

public class ErrorMessages {
    
    // Extension 관련
    public static final String EXTENSION_NOT_FOUND = "확장자를 찾을 수 없습니다";
    public static final String EXTENSION_DUPLICATE = "이미 등록된 확장자입니다.";
    public static final String EXTENSION_MAX_COUNT = "커스텀 확장자는 최대 200개까지만 추가 가능합니다";
    public static final String EXTENSION_INVALID_FORMAT = "확장자 형식이 올바르지 않습니다 (영문, 숫자만 가능)";
    public static final String EXTENSION_TOO_LONG = "확장자는 최대 20자까지 입력 가능합니다";
    public static final String EXTENSION_EMPTY = "확장자를 입력해주세요";
    public static final String EXTENSION_FIXED_ONLY = "고정 확장자만 차단 상태를 변경할 수 있습니다";
    public static final String EXTENSION_FIXED_DELETE = "고정 확장자는 삭제할 수 없습니다";
    public static final String CANNOT_DELETE_FIXED = "고정 확장자는 삭제할 수 없습니다.";
    public static final String MAX_CUSTOM_EXTENSIONS = "커스텀 확장자는 최대 200개까지만 추가할 수 있습니다.";
    public static final String DUPLICATE_EXTENSION = "이미 등록된 확장자입니다.";
    public static final String EXTENSION_BLOCKED = "차단된 확장자입니다";
    
    // File 관련
    public static final String FILE_EMPTY = "파일이 비어있습니다";
    public static final String FILE_NO_EXTENSION = "확장자가 없는 파일입니다";
    public static final String FILE_UPLOAD_FAILED = "파일 업로드에 실패했습니다";
    
    // Auth 관련
    public static final String AUTH_REQUIRED = "로그인이 필요한 서비스입니다";
    public static final String AUTH_UNAUTHORIZED = "권한이 없습니다";
    
    private ErrorMessages() {
        throw new AssertionError("상수 클래스는 인스턴스화할 수 없습니다");
    }
}
