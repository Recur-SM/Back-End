package com.seolstudy.backend.domain.auth.repository;

import com.seolstudy.backend.domain.auth.entity.RefreshToken;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByUserId(Long userId);
    Optional<RefreshToken> findByToken(String token);
    void deleteByUserId(Long userId);
}
