package com.example.cinema.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception cho các lỗi xác thực và bảo mật (JWT, API Key, v.v.)
 * HTTP Status: 401 Unauthorized
 */
public class AuthException extends ServiceException {

    private static final String SERVICE_NAME = "AUTH";

    public AuthException(String errorCode, String message) {
        super(SERVICE_NAME, errorCode, message, HttpStatus.UNAUTHORIZED);
    }

    public AuthException(String errorCode, String message, Throwable cause) {
        super(SERVICE_NAME, errorCode, message, HttpStatus.UNAUTHORIZED, cause);
    }

    // --- Factory Methods ---

    /** JWT đã hết hạn, yêu cầu refresh token hoặc đăng nhập lại */
    public static AuthException tokenExpired() {
        return new AuthException("TOKEN_EXPIRED", "Phien lam viec da het han, vui long dang nhap lai");
    }

    /** JWT không hợp lệ (bị sửa, sai chữ ký, v.v.) */
    public static AuthException tokenInvalid() {
        return new AuthException("TOKEN_INVALID", "Token khong hop le");
    }

    /** Token đã bị blacklist (người dùng đã logout) */
    public static AuthException tokenBlacklisted() {
        return new AuthException("TOKEN_BLACKLISTED", "Phien dang nhap da ket thuc (token bi thu hoi)");
    }

    /** Phát hiện truy cập bằng token cũ (vi phạm Single-Session) */
    public static AuthException sessionInvalidated(String userId) {
        return new AuthException("SESSION_INVALIDATED",
                "Phien lam viec tren thiet bi nay da bi thay the. Vui long dang nhap lai. [UserID: " + userId + "]");
    }
}
