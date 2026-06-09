package com.example.cinema.common.handler;

import java.time.ZonedDateTime;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorResponse {
    private ZonedDateTime timestamp;
    private int status;
    private String error;
    private String message;
    private String path;
    private String service;    // Ten service phat sinh loi (VD: CATALOG, SCHEDULING)
    private String errorCode;  // Ma loi rieng (VD: MOVIE_NOT_FOUND, SHOWTIME_CONFLICT)
}
