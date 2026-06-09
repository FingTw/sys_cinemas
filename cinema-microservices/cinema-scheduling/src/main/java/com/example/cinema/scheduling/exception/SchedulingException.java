package com.example.cinema.scheduling.exception;

import com.example.cinema.common.exception.ServiceException;
import org.springframework.http.HttpStatus;

/**
 * Exception riêng cho Scheduling Service (Suất chiếu).
 */
public class SchedulingException extends ServiceException {

    private static final String SERVICE_NAME = "SCHEDULING";

    public SchedulingException(String errorCode, String message, HttpStatus status) {
        super(SERVICE_NAME, errorCode, message, status);
    }

    public SchedulingException(String errorCode, String message, HttpStatus status, Throwable cause) {
        super(SERVICE_NAME, errorCode, message, status, cause);
    }

    // --- Factory Methods ---

    public static SchedulingException showtimeNotFound(String showtimeId) {
        return new SchedulingException("SHOWTIME_NOT_FOUND",
                "Suat chieu khong ton tai voi ID: " + showtimeId, HttpStatus.NOT_FOUND);
    }

    public static SchedulingException showtimeConflict(String details) {
        return new SchedulingException("SHOWTIME_CONFLICT", details, HttpStatus.CONFLICT);
    }

    public static SchedulingException movieNotAvailable(String movieId) {
        return new SchedulingException("MOVIE_NOT_AVAILABLE",
                "Phim khong ton tai voi ID: " + movieId, HttpStatus.NOT_FOUND);
    }

    public static SchedulingException roomNotAvailable(String roomId) {
        return new SchedulingException("ROOM_NOT_AVAILABLE",
                "Phong chieu khong ton tai voi ID: " + roomId, HttpStatus.NOT_FOUND);
    }

    public static SchedulingException createFailed(String reason, Throwable cause) {
        return new SchedulingException("SHOWTIME_CREATE_FAILED",
                "Loi khi tao suat chieu: " + reason, HttpStatus.INTERNAL_SERVER_ERROR, cause);
    }

    public static SchedulingException databaseError(String operation, Throwable cause) {
        return new SchedulingException("DB_ERROR",
                "Loi he thong trong thao tac: " + operation, HttpStatus.INTERNAL_SERVER_ERROR, cause);
    }
}
