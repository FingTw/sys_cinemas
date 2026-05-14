package com.example.cinema.infrastructure.database.repositories;

import com.example.cinema.infrastructure.database.entities.TokenBlacklist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;

@Repository
public interface TokenBlacklistRepository extends JpaRepository<TokenBlacklist, String> {
    
    @Modifying
    @Query("DELETE FROM TokenBlacklist t WHERE t.expiresAt < :now")
    void deleteExpiredTokens(Date now);
}
