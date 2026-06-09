package com.example.cinema.iam.exception;

import com.example.cinema.common.exception.ServiceException;
import org.springframework.http.HttpStatus;

/**
 * Exception rieng cho IAM Service.
 * Moi loi nghiep vu trong IAM deu dung class nay.
 * Thong diep tra ve khong dau de tranh loi encoding va dong nhat voi backend standards.
 */
public class IamException extends ServiceException {

    private static final String SERVICE_NAME = "IAM";

    public IamException(String errorCode, String message, HttpStatus status) {
        super(SERVICE_NAME, errorCode, message, status);
    }

    public IamException(String errorCode, String message, HttpStatus status, Throwable cause) {
        super(SERVICE_NAME, errorCode, message, status, cause);
    }

    // --- Factory Methods ---

    public static IamException userNotFound(String userId) {
        return new IamException("USER_NOT_FOUND",
                "Nguoi dung khong ton tai: " + userId, HttpStatus.NOT_FOUND);
    }

    public static IamException authenticationFailed(String reason) {
        return new IamException("AUTH_FAILED",
                "Xac thuc that bai: " + reason, HttpStatus.UNAUTHORIZED);
    }

    public static IamException duplicateUser(String username) {
        return new IamException("DUPLICATE_USER",
                "Ten dang nhap da duoc su dung: " + username, HttpStatus.CONFLICT);
    }

    public static IamException registrationFailed(String reason, Throwable cause) {
        return new IamException("REGISTRATION_FAILED",
                "Dang ky that bai: " + reason, HttpStatus.BAD_REQUEST, cause);
    }

    public static IamException accessDenied(String resource) {
        return new IamException("ACCESS_DENIED",
                "Khong co quyen truy cap: " + resource, HttpStatus.FORBIDDEN);
    }

    public static IamException databaseError(String operation, Throwable cause) {
        return new IamException("DB_ERROR",
                "Loi he thong trong thao tac: " + operation, HttpStatus.INTERNAL_SERVER_ERROR, cause);
    }
}
