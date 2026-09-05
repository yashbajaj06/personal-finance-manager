package com.finance.manager.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents an income or expense category. Includes the seven system-provided
 * default categories (Salary, Food, Rent, Transportation, Entertainment,
 * Healthcare, Utilities) as well as per-user custom categories.
 */
@Entity
@Table(name = "categories")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    @Builder.Default
    private boolean isCustom = false;

    /**
     * @return {@code true} if this is a system default category that cannot
     *         be modified or deleted by any user.
     */
    public boolean isDefault() {
        return !isCustom;
    }
}
