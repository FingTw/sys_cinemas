package com.example.cinema.facility.exception;

import com.example.cinema.common.exception.ServiceException;
import org.springframework.http.HttpStatus;

/**
 * Exception riêng cho Facility Service (Phòng, Ghế).
 */
public class FacilityException extends ServiceException {

    private static final String SERVICE_NAME = "FACILITY";

    public FacilityException(String errorCode, String message, HttpStatus status) {
        super(SERVICE_NAME, errorCode, message, status);
    }

    public FacilityException(String errorCode, String message, HttpStatus status, Throwable cause) {
        super(SERVICE_NAME, errorCode, message, status, cause);
    }

    // --- Factory Methods ---

    public static FacilityException roomNotFound(String roomId) {
        return new FacilityException("ROOM_NOT_FOUND",
                "Phong chieu khong ton tai voi ID: " + roomId, HttpStatus.NOT_FOUND);
    }

    public static FacilityException seatNotFound(String seatId) {
        return new FacilityException("SEAT_NOT_FOUND",
                "Ghe khong ton tai voi ID: " + seatId, HttpStatus.NOT_FOUND);
    }

    public static FacilityException roomCreateFailed(String name, Throwable cause) {
        return new FacilityException("ROOM_CREATE_FAILED",
                "Loi khi tao phong [" + name + "]: " + cause.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR, cause);
    }

    public static FacilityException roomDeleteFailed(String roomId, Throwable cause) {
        return new FacilityException("ROOM_DELETE_FAILED",
                "Loi khi xoa phong [" + roomId + "]: " + cause.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR, cause);
    }

    public static FacilityException seatUpdateFailed(String seatId, Throwable cause) {
        return new FacilityException("SEAT_UPDATE_FAILED",
                "Loi khi cap nhat ghe [" + seatId + "]: " + cause.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR, cause);
    }

    public static FacilityException databaseError(String operation, Throwable cause) {
        return new FacilityException("DB_ERROR",
                "Loi he thong trong thao tac: " + operation, HttpStatus.INTERNAL_SERVER_ERROR, cause);
    }
}
