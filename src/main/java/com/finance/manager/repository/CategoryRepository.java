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
 * Data access for {@link Category} entities, covering both system default
 * categories (no owning user) and per-user custom categories.
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    /**
     * @return all system default categories (Salary, Food, Rent, etc.).
     */
    List<Category> findByUserIsNull();

    /**
     * @return all custom categories belonging to the given user.
     */
    List<Category> findByUser(User user);

    /**
     * Resolves a category by name that is either a system default or owned
     * by the given user, for validating a transaction's category reference.
     */
    @Query("SELECT c FROM Category c WHERE c.name = :name AND (c.user = :user OR c.user IS NULL)")
    Optional<Category> findByNameAndUserOrDefault(@Param("name") String name, @Param("user") User user);

    /**
     * Looks up a custom category by name, scoped to the given user.
     */
    Optional<Category> findByNameAndUser(String name, User user);

    /**
     * @return {@code true} if the given user already has a custom category
     *         with this name.
     */
    boolean existsByNameAndUser(String name, User user);

    /**
     * Looks up a system default category by name.
     */
    Optional<Category> findByNameAndUserIsNull(String name);
}
