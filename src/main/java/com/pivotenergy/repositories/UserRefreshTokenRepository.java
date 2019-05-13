package com.pivotenergy.repositories;

import com.pivotenergy.domain.UserRefreshToken;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.Optional;

@Repository
public interface UserRefreshTokenRepository extends CrudRepository<UserRefreshToken, Long> {
    Optional<UserRefreshToken> findByToken(String token);

    @Transactional
    @Modifying
    @Query("DELETE FROM UserRefreshToken o WHERE o.expiresAt < CURRENT_TIMESTAMP")
    void purgeExpiredTokens();
}
