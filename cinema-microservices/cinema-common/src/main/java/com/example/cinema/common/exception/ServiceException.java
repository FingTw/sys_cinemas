package com.example.cinema.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception cơ sở cho toàn bộ hệ thống microservices.
 *
 * Mỗi service con kế thừa và tạo factory methods riêng, ví dụ:
 *   - IamException.userNotFound(id)
 *   - CatalogException.movieNotFound(id)
 *   - SchedulingException.showtimeConflict(msg)
 *
 * HttpStatus được đưa vào đây để GlobalExceptionHandler không cần
 * phán đoán status từ errorCode nữa — rõ ràng và chắc chắn hơn.
 */
public abstract class ServiceException extends RuntimeException {

    private final String serviceName;
    private final String errorCode;
    private final HttpStatus httpStatus;

    protected ServiceException(String serviceName, String errorCode, String message, HttpStatus httpStatus) {
        super(message);
        this.serviceName = serviceName;
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }

    protected ServiceException(String serviceName, String errorCode, String message, HttpStatus httpStatus, Throwable cause) {
        super(message, cause);
        this.serviceName = serviceName;
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    @Override
    public String toString() {
        return String.format("[%s] %s (%d) - %s", serviceName, errorCode, httpStatus.value(), getMessage());
    }
}
