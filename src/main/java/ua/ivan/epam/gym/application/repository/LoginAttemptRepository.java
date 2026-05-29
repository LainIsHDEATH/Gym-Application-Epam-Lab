package ua.ivan.epam.gym.application.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ua.ivan.epam.gym.application.model.LoginAttempt;

import java.util.Optional;

@Repository
public interface LoginAttemptRepository extends JpaRepository<LoginAttempt, String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT attempt
        FROM LoginAttempt attempt
        WHERE attempt.user.username = :username
        """)
    Optional<LoginAttempt> findByUsernameForUpdate(@Param("username") String username);

}