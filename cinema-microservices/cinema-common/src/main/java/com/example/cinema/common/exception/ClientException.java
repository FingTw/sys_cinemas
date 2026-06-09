package com.example.cinema.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception cho các lỗi nghiệp vụ do phía Client gây ra.
 *
 * Dùng khi: dữ liệu đầu vào sai, tài nguyên không tìm thấy, xung đột nghiệp vụ.
 *
 * Nếu service đã có XxxException riêng (IamException, BookingException, v.v.)
 * thì ưu tiên dùng loại đó thay vì ClientException.
 *
 * Constructor ngắn (chỉ message) mặc định dùng HTTP 400 — tiện cho code cũ.
 */
public class ClientException extends ServiceException {

    private static final String DEFAULT_SERVICE = "APP";

    /** Constructor tương thích ngược — mặc định HTTP 400 */
    public ClientException(String message) {
        super(DEFAULT_SERVICE, "CLIENT_ERROR", message, HttpStatus.BAD_REQUEST);
    }

    /** Constructor tương thích ngược — mặc định HTTP 400, có cause */
    public ClientException(String message, Throwable cause) {
        super(DEFAULT_SERVICE, "CLIENT_ERROR", message, HttpStatus.BAD_REQUEST, cause);
    }

    /** Constructor đầy đủ */
    public ClientException(String serviceName, String errorCode, String message, HttpStatus status) {
        super(serviceName, errorCode, message, status);
    }

    /** Constructor đầy đủ với cause */
    public ClientException(String serviceName, String errorCode, String message, HttpStatus status, Throwable cause) {
        super(serviceName, errorCode, message, status, cause);
    }

    // --- Factory Methods ---

    public static ClientException badRequest(String serviceName, String errorCode, String message) {
        return new ClientException(serviceName, errorCode, message, HttpStatus.BAD_REQUEST);
    }

    public static ClientException notFound(String serviceName, String errorCode, String message) {
        return new ClientException(serviceName, errorCode, message, HttpStatus.NOT_FOUND);
    }

    public static ClientException conflict(String serviceName, String errorCode, String message) {
        return new ClientException(serviceName, errorCode, message, HttpStatus.CONFLICT);
    }

    public static ClientException forbidden(String serviceName, String errorCode, String message) {
        return new ClientException(serviceName, errorCode, message, HttpStatus.FORBIDDEN);
    }
}
