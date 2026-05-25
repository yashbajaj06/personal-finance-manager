package com.finance.manager.repository;

import com.finance.manager.entity.Category;
import com.finance.manager.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Category entity operations.
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    // Default categories (user is null)
    List<Category> findByUserIsNull();

    // Custom categories for a specific user
    List<Category> findByUser(User user);

    // Find category by name for a user (custom) or default
    @Query("SELECT c FROM Category c WHERE c.name = :name AND (c.user = :user OR c.user IS NULL)")
    Optional<Category> findByNameAndUserOrDefault(@Param("name") String name, @Param("user") User user);

    // Find custom category by name for a specific user
    Optional<Category> findByNameAndUser(String name, User user);

    // Check if custom category name exists for user
    boolean existsByNameAndUser(String name, User user);

    // Find by name (default category)
    Optional<Category> findByNameAndUserIsNull(String name);
}
