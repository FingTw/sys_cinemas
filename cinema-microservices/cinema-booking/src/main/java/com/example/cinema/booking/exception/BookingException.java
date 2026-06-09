package com.example.cinema.booking.exception;

import com.example.cinema.common.exception.ServiceException;
import org.springframework.http.HttpStatus;

/**
 * Exception riêng cho Booking Service (Đặt vé, Thanh toán).
 */
public class BookingException extends ServiceException {

    private static final String SERVICE_NAME = "BOOKING";

    public BookingException(String errorCode, String message, HttpStatus status) {
        super(SERVICE_NAME, errorCode, message, status);
    }

    public BookingException(String errorCode, String message, HttpStatus status, Throwable cause) {
        super(SERVICE_NAME, errorCode, message, status, cause);
    }

    // --- Factory Methods ---

    public static BookingException bookingNotFound(String bookingId) {
        return new BookingException("BOOKING_NOT_FOUND",
                "Don dat ve khong ton tai voi ID: " + bookingId, HttpStatus.NOT_FOUND);
    }

    public static BookingException seatAlreadyBooked(String seatInfo) {
        return new BookingException("SEAT_ALREADY_BOOKED",
                "Ghe da duoc dat: " + seatInfo, HttpStatus.CONFLICT);
    }

    public static BookingException paymentFailed(String reason) {
        return new BookingException("PAYMENT_FAILED",
                "Thanh toan that bai: " + reason, HttpStatus.BAD_REQUEST);
    }

    public static BookingException paymentFailed(String reason, Throwable cause) {
        return new BookingException("PAYMENT_FAILED",
                "Thanh toan that bai: " + reason, HttpStatus.BAD_REQUEST, cause);
    }

    public static BookingException createFailed(String reason, Throwable cause) {
        return new BookingException("BOOKING_CREATE_FAILED",
                "Loi khi tao don dat ve: " + reason, HttpStatus.INTERNAL_SERVER_ERROR, cause);
    }

    public static BookingException databaseError(String operation, Throwable cause) {
        return new BookingException("DB_ERROR",
                "Loi he thong trong thao tac: " + operation, HttpStatus.INTERNAL_SERVER_ERROR, cause);
    }
}
