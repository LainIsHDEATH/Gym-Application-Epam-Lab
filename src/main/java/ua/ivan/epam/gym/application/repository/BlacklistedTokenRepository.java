package ua.ivan.epam.gym.application.repository;

import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;
import ua.ivan.epam.gym.application.model.BlacklistedToken;

import java.time.Instant;

@Repository
public interface BlacklistedTokenRepository extends JpaRepository<BlacklistedToken, String> {

    boolean existsByTokenHashAndExpiresAtAfter(String tokenHash, Instant now);

}