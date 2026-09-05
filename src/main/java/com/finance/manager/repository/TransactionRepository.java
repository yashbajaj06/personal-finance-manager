package com.finance.manager.repository;

import com.finance.manager.entity.Category;
import com.finance.manager.entity.Transaction;
import com.finance.manager.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Data access for {@link Transaction} entities. Extends
 * {@link JpaSpecificationExecutor} so that the optional date/category/type
 * filters on {@code GET /api/transactions} can be combined freely via
 * {@link TransactionSpecifications}, instead of one derived-query method
 * per filter combination.
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long>, JpaSpecificationExecutor<Transaction> {

    /**
     * Looks up a single transaction by id, scoped to the given user, so
     * that one user can never retrieve or modify another user's transaction.
     */
    Optional<Transaction> findByIdAndUser(Long id, User user);

    /**
     * @return {@code true} if any transaction still references this category,
     *         used to block deletion of in-use custom categories.
     */
    boolean existsByCategory(Category category);

    /**
     * Sums all INCOME transaction amounts for the user on or after the given date.
     */
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
           "WHERE t.user = :user AND t.date >= :startDate AND t.category.type = 'INCOME'")
    BigDecimal sumIncomeByUserSinceDate(@Param("user") User user, @Param("startDate") LocalDate startDate);

    /**
     * Sums all EXPENSE transaction amounts for the user on or after the given date.
     */
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
           "WHERE t.user = :user AND t.date >= :startDate AND t.category.type = 'EXPENSE'")
    BigDecimal sumExpensesByUserSinceDate(@Param("user") User user, @Param("startDate") LocalDate startDate);

    /**
     * @return all of the user's transactions falling within the given month/year,
     *         used to build the monthly report.
     */
    @Query("SELECT t FROM Transaction t WHERE t.user = :user " +
           "AND YEAR(t.date) = :year AND MONTH(t.date) = :month")
    List<Transaction> findByUserAndYearAndMonth(
            @Param("user") User user, @Param("year") int year, @Param("month") int month);

    /**
     * @return all of the user's transactions falling within the given year,
     *         used to build the yearly report.
     */
    @Query("SELECT t FROM Transaction t WHERE t.user = :user AND YEAR(t.date) = :year")
    List<Transaction> findByUserAndYear(@Param("user") User user, @Param("year") int year);
}
