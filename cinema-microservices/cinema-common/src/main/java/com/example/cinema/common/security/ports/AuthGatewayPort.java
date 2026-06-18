package com.example.cinema.common.security.ports;

/**
 * AuthGatewayPort - Dinh nghia hop dong giao tiep voi Keycloak (Standalone SSO).
 *
 * <p>Keycloak giu DB nguoi dung rieng. Backend chi tuong tac khi:
 * <ul>
 *   <li>Validate Keycloak JWT gui tu Frontend (offline, qua JWKS).</li>
 *   <li>Goi Keycloak logout khi nguoi dung dang xuat.</li>
 *   <li>Xac thuc truc tiep username/password (legacy, dung cho admin tool).</li>
 * </ul>
 */
public interface AuthGatewayPort {

    /**
     * Xac thuc truc tiep username/password (Resource Owner Password Credentials).
     * Chi dung cho quan tri vien hoac kiem tra noi bo.
     */
    boolean verifyCredentials(String username, String password);

    /**
     * Validate Keycloak JWT offline bang JWKS (khong can goi mang toi Keycloak).
     * Spring cache JWKS tu dong sau lan fetch dau tien.
     *
     * @param keycloakJwt Access Token hoac ID Token tu Keycloak.
     * @return {@link SsoUserInfo} chua username, email, sub cua nguoi dung.
     * @throws RuntimeException neu token het han, chu ky sai, hoac Keycloak issuer khong khop.
     */
    SsoUserInfo validateKeycloakToken(String keycloakJwt);

    /**
     * Goi Keycloak backchannel logout de huy session cua nguoi dung.
     * Su dung Keycloak Refresh Token da luu tu truoc khi dang nhap SSO.
     *
     * @param keycloakRefreshToken Keycloak Refresh Token (luu trong Redis luc SSO login).
     */
    void logoutFromKeycloak(String keycloakRefreshToken);
}
