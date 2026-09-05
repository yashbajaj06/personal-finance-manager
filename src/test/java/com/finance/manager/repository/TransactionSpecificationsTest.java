package com.finance.manager.repository;

import com.finance.manager.entity.Category;
import com.finance.manager.entity.Transaction;
import com.finance.manager.entity.TransactionType;
import com.finance.manager.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
class TransactionSpecificationsTest {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    private User user;
    private Category salary;
    private Category food;

    @BeforeEach
    void setUp() {
        user = userRepository.save(User.builder()
                .username("test@example.com")
                .password("pw")
                .fullName("Test User")
                .phoneNumber("+1234567890")
                .build());

        salary = categoryRepository.save(Category.builder()
                .name("Salary").type(TransactionType.INCOME).isCustom(false).build());

        food = categoryRepository.save(Category.builder()
                .name("Food").type(TransactionType.EXPENSE).isCustom(false).build());

        transactionRepository.save(Transaction.builder()
                .amount(new BigDecimal("5000")).date(LocalDate.now().minusDays(10))
                .category(salary).user(user).build());

        transactionRepository.save(Transaction.builder()
                .amount(new BigDecimal("200")).date(LocalDate.now().minusDays(5))
                .category(food).user(user).build());

        transactionRepository.save(Transaction.builder()
                .amount(new BigDecimal("100")).date(LocalDate.now())
                .category(food).user(user).build());
    }

    @Test
    void hasType_FiltersByIncome() {
        Specification<Transaction> spec = Specification
                .where(TransactionSpecifications.forUser(user))
                .and(TransactionSpecifications.hasType(TransactionType.INCOME));

        List<Transaction> result = transactionRepository.findAll(spec);

        assertEquals(1, result.size());
        assertEquals("Salary", result.get(0).getCategory().getName());
    }

    @Test
    void hasType_NullReturnsAll() {
        Specification<Transaction> spec = Specification
                .where(TransactionSpecifications.forUser(user))
                .and(TransactionSpecifications.hasType(null));

        List<Transaction> result = transactionRepository.findAll(spec);

        assertEquals(3, result.size());
    }

    @Test
    void hasCategory_FiltersByCategory() {
        Specification<Transaction> spec = Specification
                .where(TransactionSpecifications.forUser(user))
                .and(TransactionSpecifications.hasCategory(food));

        List<Transaction> result = transactionRepository.findAll(spec);

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(t -> t.getCategory().getName().equals("Food")));
    }

    @Test
    void dateBetween_FiltersByRange() {
        Specification<Transaction> spec = Specification
                .where(TransactionSpecifications.forUser(user))
                .and(TransactionSpecifications.dateBetween(LocalDate.now().minusDays(6), LocalDate.now().minusDays(1)));

        List<Transaction> result = transactionRepository.findAll(spec);

        assertEquals(1, result.size());
        assertEquals(new BigDecimal("200"), result.get(0).getAmount());
    }

    @Test
    void dateBetween_OnlyStartDate() {
        Specification<Transaction> spec = Specification
                .where(TransactionSpecifications.forUser(user))
                .and(TransactionSpecifications.dateBetween(LocalDate.now().minusDays(6), null));

        List<Transaction> result = transactionRepository.findAll(spec);

        assertEquals(2, result.size());
    }

    @Test
    void dateBetween_OnlyEndDate() {
        Specification<Transaction> spec = Specification
                .where(TransactionSpecifications.forUser(user))
                .and(TransactionSpecifications.dateBetween(null, LocalDate.now().minusDays(6)));

        List<Transaction> result = transactionRepository.findAll(spec);

        assertEquals(1, result.size());
        assertEquals("Salary", result.get(0).getCategory().getName());
    }

    @Test
    void combinedFilters_SortedNewestFirst() {
        Specification<Transaction> spec = Specification
                .where(TransactionSpecifications.forUser(user))
                .and(TransactionSpecifications.hasType(TransactionType.EXPENSE))
                .and(TransactionSpecifications.hasCategory(food));

        List<Transaction> result = transactionRepository.findAll(spec, Sort.by(Sort.Direction.DESC, "date"));

        assertEquals(2, result.size());
        assertTrue(result.get(0).getDate().isAfter(result.get(1).getDate())
                || result.get(0).getDate().isEqual(result.get(1).getDate()));
    }
}
