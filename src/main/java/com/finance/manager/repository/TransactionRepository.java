package com.finance.manager.repository;

import com.finance.manager.entity.Category;
import com.finance.manager.entity.Transaction;
import com.finance.manager.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Transaction entity operations.
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    // Get all transactions for a user, newest first
    List<Transaction> findByUserOrderByDateDesc(User user);

    // Filter by date range
    List<Transaction> findByUserAndDateBetweenOrderByDateDesc(User user, LocalDate start, LocalDate end);

    // Filter by category
    List<Transaction> findByUserAndCategoryOrderByDateDesc(User user, Category category);

    // Filter by date range and category
    List<Transaction> findByUserAndDateBetweenAndCategoryOrderByDateDesc(
            User user, LocalDate start, LocalDate end, Category category);

    // Find by id and user (for security - ensure user owns the transaction)
    Optional<Transaction> findByIdAndUser(Long id, User user);

    // Check if any transaction uses this category
    boolean existsByCategory(Category category);

    // For savings goals: sum income/expenses since a date for a user
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
           "WHERE t.user = :user AND t.date >= :startDate AND t.category.type = 'INCOME'")
    java.math.BigDecimal sumIncomeByUserSinceDate(@Param("user") User user, @Param("startDate") LocalDate startDate);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
           "WHERE t.user = :user AND t.date >= :startDate AND t.category.type = 'EXPENSE'")
    java.math.BigDecimal sumExpensesByUserSinceDate(@Param("user") User user, @Param("startDate") LocalDate startDate);

    // For monthly reports
    @Query("SELECT t FROM Transaction t WHERE t.user = :user " +
           "AND YEAR(t.date) = :year AND MONTH(t.date) = :month")
    List<Transaction> findByUserAndYearAndMonth(
            @Param("user") User user, @Param("year") int year, @Param("month") int month);

    // For yearly reports
    @Query("SELECT t FROM Transaction t WHERE t.user = :user AND YEAR(t.date) = :year")
    List<Transaction> findByUserAndYear(@Param("user") User user, @Param("year") int year);
}
