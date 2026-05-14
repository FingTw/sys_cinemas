package com.example.cinema.application.usecases;

import com.example.cinema.application.ports.in.RevokeTokenUseCase;
import com.example.cinema.application.ports.out.TokenCachePort;
import com.example.cinema.application.ports.out.TokenRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RevokeTokenService implements RevokeTokenUseCase {

    private final TokenCachePort tokenCachePort;
    private final TokenRepositoryPort tokenRepositoryPort;

    public RevokeTokenService(TokenCachePort tokenCachePort, TokenRepositoryPort tokenRepositoryPort) {
        this.tokenCachePort = tokenCachePort;
        this.tokenRepositoryPort = tokenRepositoryPort;
    }

    @Override
    @Transactional
    public void revokeAllTokensForUser(String userId) {
        // 1. Tăng token_version trong DB và Redis
        // Thao tác này sẽ làm VÔ HIỆU HÓA NGAY LẬP TỨC toàn bộ Access Token đang trôi nổi (vì khác version)
        tokenCachePort.incrementTokenVersion(userId);
        
        // 2. Mark is_active = false cho toàn bộ Refresh Tokens của User trong bảng auth_tokens
        tokenRepositoryPort.revokeAllByUserId(userId);
    }

    @Override
    @Transactional
    public void revokeSingleToken(String jti) {
        // Chỉ vô hiệu hóa 1 Refresh Token cụ thể bằng JTI
        tokenRepositoryPort.revokeTokenByJti(jti);
    }
}
