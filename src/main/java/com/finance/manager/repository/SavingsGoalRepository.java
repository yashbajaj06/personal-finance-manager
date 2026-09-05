package com.finance.manager.repository;

import com.finance.manager.entity.SavingsGoal;
import com.finance.manager.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Data access for {@link SavingsGoal} entities.
 */
@Repository
public interface SavingsGoalRepository extends JpaRepository<SavingsGoal, Long> {

    /**
     * @return all savings goals belonging to the given user.
     */
    List<SavingsGoal> findByUser(User user);

    /**
     * Looks up a single savings goal by id, scoped to the given user, so
     * that one user can never retrieve another user's goal by id.
     */
    Optional<SavingsGoal> findByIdAndUser(Long id, User user);
}
