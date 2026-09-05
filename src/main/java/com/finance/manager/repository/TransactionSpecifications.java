package com.finance.manager.repository;

import com.finance.manager.entity.Category;
import com.finance.manager.entity.Transaction;
import com.finance.manager.entity.TransactionType;
import com.finance.manager.entity.User;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

/**
 * Reusable, composable JPA {@link Specification} predicates for filtering
 * transactions on {@code GET /api/transactions}. Each method returns a
 * no-op (match-all) predicate when its filter argument is {@code null}, so
 * any combination of filters can be chained together with
 * {@link Specification#and(Specification)}.
 */
public final class TransactionSpecifications {

    private TransactionSpecifications() {
    }

    /**
     * Restricts results to transactions owned by the given user.
     */
    public static Specification<Transaction> forUser(User user) {
        return (root, query, cb) -> cb.equal(root.get("user"), user);
    }

    /**
     * Restricts results to transactions dated within [startDate, endDate].
     * Either bound may be {@code null} for an open-ended range; if both are
     * {@code null}, no date filtering is applied.
     */
    public static Specification<Transaction> dateBetween(LocalDate startDate, LocalDate endDate) {
        return (root, query, cb) -> {
            if (startDate != null && endDate != null) {
                return cb.between(root.get("date"), startDate, endDate);
            }
            if (startDate != null) {
                return cb.greaterThanOrEqualTo(root.get("date"), startDate);
            }
            if (endDate != null) {
                return cb.lessThanOrEqualTo(root.get("date"), endDate);
            }
            return cb.conjunction();
        };
    }

    /**
     * Restricts results to transactions in the given category, or matches
     * all transactions if the category is {@code null}.
     */
    public static Specification<Transaction> hasCategory(Category category) {
        return (root, query, cb) -> {
            if (category == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("category"), category);
        };
    }

    /**
     * Restricts results to transactions whose category is of the given
     * {@link TransactionType} (INCOME or EXPENSE), or matches all
     * transactions if the type is {@code null}.
     */
    public static Specification<Transaction> hasType(TransactionType type) {
        return (root, query, cb) -> {
            if (type == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("category").get("type"), type);
        };
    }
}
