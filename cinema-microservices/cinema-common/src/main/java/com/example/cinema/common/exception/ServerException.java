package com.example.cinema.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception cho các lỗi hệ thống / server-side (lỗi DB, lỗi kết nối external service, v.v.)
 * HTTP Status: 500 Internal Server Error (mặc định).
 *
 * Constructor ngắn (message, cause) tương thích với code cũ — mặc định HTTP 500.
 */
public class ServerException extends ServiceException {

    private static final String DEFAULT_SERVICE = "APP";

    /** Constructor tương thích ngược — mặc định HTTP 500 */
    public ServerException(String message) {
        super(DEFAULT_SERVICE, "SERVER_ERROR", message, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /** Constructor tương thích ngược — mặc định HTTP 500, có cause */
    public ServerException(String message, Throwable cause) {
        super(DEFAULT_SERVICE, "SERVER_ERROR", message, HttpStatus.INTERNAL_SERVER_ERROR, cause);
    }

    /** Constructor đầy đủ */
    public ServerException(String serviceName, String errorCode, String message, HttpStatus status) {
        super(serviceName, errorCode, message, status);
    }

    /** Constructor đầy đủ với cause */
    public ServerException(String serviceName, String errorCode, String message, HttpStatus status, Throwable cause) {
        super(serviceName, errorCode, message, status, cause);
    }

    // --- Factory Methods ---

    public static ServerException internal(String serviceName, String errorCode, String message, Throwable cause) {
        return new ServerException(serviceName, errorCode, message, HttpStatus.INTERNAL_SERVER_ERROR, cause);
    }

    public static ServerException serviceUnavailable(String serviceName, String errorCode, String message, Throwable cause) {
        return new ServerException(serviceName, errorCode, message, HttpStatus.SERVICE_UNAVAILABLE, cause);
    }
}
