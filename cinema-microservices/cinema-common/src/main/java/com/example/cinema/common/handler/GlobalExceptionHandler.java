package com.example.cinema.common.handler;

import com.example.cinema.common.exception.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Global Exception Handler — điểm xử lý lỗi tập trung duy nhất.
 *
 * Thứ tự ưu tiên (từ cụ thể đến tổng quát):
 *  1. ServiceException (và mọi subclass: AuthException, ClientException, ServerException, IamException, ...)
 *  2. MethodArgumentNotValidException (lỗi @Valid)
 *  3. AccessDeniedException (Spring Security 403)
 *  4. Exception (fallback cuối cùng)
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @Value("${spring.application.name}")
    private String serviceName;

    // =========================================================================
    // 1. ServiceException — bắt tất cả mọi custom exception trong hệ thống
    // =========================================================================
    @ExceptionHandler(ServiceException.class)
    public ResponseEntity<ErrorResponse> handleServiceException(ServiceException ex, WebRequest request) {
        String path = extractPath(request);
        HttpStatus status = ex.getHttpStatus();

        // Log level phụ thuộc vào HTTP status: 4xx -> WARN, 5xx -> ERROR
        if (status.is5xxServerError()) {
            log.error("[{}] {} | {} | {}", ex.getServiceName(), ex.getErrorCode(), path, ex.getMessage(), ex);
        } else {
            log.warn("[{}] {} | {} | {}", ex.getServiceName(), ex.getErrorCode(), path, ex.getMessage());
        }

        // Message trả về client: 5xx thì generic, 4xx thì giữ nguyên để frontend hiển thị
        String clientMessage = status.is5xxServerError()
                ? "He thong dang gap su co, vui long thu lai sau."
                : ex.getMessage();

        return ResponseEntity.status(status).body(ErrorResponse.builder()
                .timestamp(ZonedDateTime.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(clientMessage)
                .path(path)
                .service(ex.getServiceName())
                .errorCode(ex.getErrorCode())
                .build());
    }

    // =========================================================================
    // 2. Validation Error (@Valid, @NotBlank, v.v.)
    // =========================================================================
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex, WebRequest request) {
        String path = extractPath(request);
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String field = ((FieldError) error).getField();
            fieldErrors.put(field, error.getDefaultMessage());
        });

        log.warn("[{}] VALIDATION_ERROR | {} | Fields: {}", serviceName.toUpperCase(), path, fieldErrors);

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(ErrorResponse.builder()
                .timestamp(ZonedDateTime.now())
                .status(HttpStatus.UNPROCESSABLE_ENTITY.value())
                .error("Unprocessable Entity")
                .message("Du lieu dau vao khong hop le: " + fieldErrors)
                .path(path)
                .service(serviceName.toUpperCase())
                .errorCode("VALIDATION_ERROR")
                .build());
    }

    // =========================================================================
    // 3. AccessDeniedException — Spring Security 403
    // =========================================================================
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException ex, WebRequest request) {
        String path = extractPath(request);
        log.warn("[{}] ACCESS_DENIED | {}", serviceName.toUpperCase(), path);

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ErrorResponse.builder()
                .timestamp(ZonedDateTime.now())
                .status(HttpStatus.FORBIDDEN.value())
                .error("Forbidden")
                .message("Ban khong co quyen thuc hien thao tac nay.")
                .path(path)
                .service(serviceName.toUpperCase())
                .errorCode("ACCESS_DENIED")
                .build());
    }

    // =========================================================================
    // 4. Fallback — bắt mọi exception chưa được xử lý cụ thể
    // =========================================================================
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(Exception ex, WebRequest request) {
        String path = extractPath(request);
        log.error("[{}] UNHANDLED_EXCEPTION | {} | Type: {} | Message: {}",
                serviceName.toUpperCase(), path, ex.getClass().getName(), ex.getMessage(), ex);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ErrorResponse.builder()
                .timestamp(ZonedDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("Internal Server Error")
                .message("Da xay ra loi he thong khong mong muon.")
                .path(path)
                .service(serviceName.toUpperCase())
                .errorCode("SYSTEM_ERROR")
                .build());
    }

    // =========================================================================
    private String extractPath(WebRequest request) {
        return request.getDescription(false).replace("uri=", "");
    }
}
