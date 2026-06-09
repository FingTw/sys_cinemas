package com.example.cinema.catalog.exception;

import com.example.cinema.common.exception.ServiceException;
import org.springframework.http.HttpStatus;

/**
 * Exception riêng cho Catalog Service (Phim).
 */
public class CatalogException extends ServiceException {

    private static final String SERVICE_NAME = "CATALOG";

    public CatalogException(String errorCode, String message, HttpStatus status) {
        super(SERVICE_NAME, errorCode, message, status);
    }

    public CatalogException(String errorCode, String message, HttpStatus status, Throwable cause) {
        super(SERVICE_NAME, errorCode, message, status, cause);
    }

    // --- Factory Methods ---

    public static CatalogException movieNotFound(String movieId) {
        return new CatalogException("MOVIE_NOT_FOUND",
                "Phim khong ton tai voi ID: " + movieId, HttpStatus.NOT_FOUND);
    }

    public static CatalogException movieCreateFailed(String reason, Throwable cause) {
        return new CatalogException("MOVIE_CREATE_FAILED",
                "Loi khi tao phim: " + reason, HttpStatus.INTERNAL_SERVER_ERROR, cause);
    }

    public static CatalogException movieUpdateFailed(String movieId, String reason, Throwable cause) {
        return new CatalogException("MOVIE_UPDATE_FAILED",
                "Loi khi cap nhat phim [" + movieId + "]: " + reason, HttpStatus.INTERNAL_SERVER_ERROR, cause);
    }

    public static CatalogException movieDeleteFailed(String movieId, String reason, Throwable cause) {
        return new CatalogException("MOVIE_DELETE_FAILED",
                "Loi khi xoa phim [" + movieId + "]: " + reason, HttpStatus.INTERNAL_SERVER_ERROR, cause);
    }

    public static CatalogException databaseError(String operation, Throwable cause) {
        return new CatalogException("DB_ERROR",
                "Loi he thong trong thao tac: " + operation, HttpStatus.INTERNAL_SERVER_ERROR, cause);
    }
}
