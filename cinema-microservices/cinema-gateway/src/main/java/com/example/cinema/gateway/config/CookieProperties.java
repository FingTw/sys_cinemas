package com.example.cinema.gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Cấu hình tập trung cho HttpOnly Cookie Session.
 * Đọc từ application.yaml block: cinema.cookie.*
 */
@Component
@ConfigurationProperties(prefix = "cinema.cookie")
public class CookieProperties {

    private boolean secure = false;
    private String sameSite = "Lax";
    private String domain = null;  // Không set domain cho localhost — Chrome từ chối domain=localhost
    private String accessTokenName = "ACCESS_TOKEN";
    private String refreshTokenName = "REFRESH_TOKEN";
    private String csrfTokenName = "XSRF-TOKEN";
    private long accessTokenMaxAge = 3600;
    private long refreshTokenMaxAge = 2592000;

    public boolean isSecure() { return secure; }
    public void setSecure(boolean secure) { this.secure = secure; }

    public String getSameSite() { return sameSite; }
    public void setSameSite(String sameSite) { this.sameSite = sameSite; }

    public String getDomain() { return domain; }
    public void setDomain(String domain) { this.domain = domain; }

    public String getAccessTokenName() { return accessTokenName; }
    public void setAccessTokenName(String accessTokenName) { this.accessTokenName = accessTokenName; }

    public String getRefreshTokenName() { return refreshTokenName; }
    public void setRefreshTokenName(String refreshTokenName) { this.refreshTokenName = refreshTokenName; }

    public String getCsrfTokenName() { return csrfTokenName; }
    public void setCsrfTokenName(String csrfTokenName) { this.csrfTokenName = csrfTokenName; }

    public long getAccessTokenMaxAge() { return accessTokenMaxAge; }
    public void setAccessTokenMaxAge(long accessTokenMaxAge) { this.accessTokenMaxAge = accessTokenMaxAge; }

    public long getRefreshTokenMaxAge() { return refreshTokenMaxAge; }
    public void setRefreshTokenMaxAge(long refreshTokenMaxAge) { this.refreshTokenMaxAge = refreshTokenMaxAge; }
}
