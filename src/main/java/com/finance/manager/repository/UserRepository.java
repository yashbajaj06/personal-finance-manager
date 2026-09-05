package com.finance.manager.repository;

import com.finance.manager.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Data access for {@link User} entities.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Looks up a user by their username (email).
     */
    Optional<User> findByUsername(String username);

    /**
     * @return {@code true} if a user is already registered with this username.
     */
    boolean existsByUsername(String username);
}
