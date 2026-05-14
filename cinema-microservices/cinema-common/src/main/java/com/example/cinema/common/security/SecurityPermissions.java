package com.example.cinema.common.security;

public final class SecurityPermissions {

    private SecurityPermissions() {
    }

    public static final String MOVIE_READ = "MOVIE_READ";
    public static final String MOVIE_CREATE = "MOVIE_CREATE";
    public static final String MOVIE_UPDATE = "MOVIE_UPDATE";
    public static final String MOVIE_DELETE = "MOVIE_DELETE";

    public static final String SHOWTIME_READ = "SHOWTIME_READ";
    public static final String SHOWTIME_CREATE = "SHOWTIME_CREATE";
    public static final String SHOWTIME_UPDATE = "SHOWTIME_UPDATE";
    public static final String SHOWTIME_DELETE = "SHOWTIME_DELETE";

    public static final String FACILITY_READ = "FACILITY_READ";
    public static final String FACILITY_CREATE = "FACILITY_CREATE";
    public static final String FACILITY_UPDATE = "FACILITY_UPDATE";
    public static final String FACILITY_DELETE = "FACILITY_DELETE";
    public static final String FACILITY_MANAGE = "FACILITY_MANAGE";

    public static final String USER_READ = "USER_READ";
    public static final String USER_MANAGE = "USER_MANAGE";

    public static final String BOOKING_CREATE = "BOOKING_CREATE";
    public static final String BOOKING_READ = "BOOKING_READ";
    public static final String BOOKING_CANCEL = "BOOKING_CANCEL";
    public static final String PROFILE_UPDATE = "PROFILE_UPDATE";
}
