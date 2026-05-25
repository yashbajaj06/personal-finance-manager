package com.finance.manager.config;

import com.finance.manager.entity.Category;
import com.finance.manager.entity.TransactionType;
import com.finance.manager.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Seeds the database with default categories on startup if they don't exist.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final CategoryRepository categoryRepository;

    @Override
    public void run(String... args) {
        List<Category> existingDefaults = categoryRepository.findByUserIsNull();
        if (existingDefaults.isEmpty()) {
            log.info("Seeding default categories...");
            List<Category> defaults = List.of(
                Category.builder().name("Salary").type(TransactionType.INCOME).isCustom(false).build(),
                Category.builder().name("Food").type(TransactionType.EXPENSE).isCustom(false).build(),
                Category.builder().name("Rent").type(TransactionType.EXPENSE).isCustom(false).build(),
                Category.builder().name("Transportation").type(TransactionType.EXPENSE).isCustom(false).build(),
                Category.builder().name("Entertainment").type(TransactionType.EXPENSE).isCustom(false).build(),
                Category.builder().name("Healthcare").type(TransactionType.EXPENSE).isCustom(false).build(),
                Category.builder().name("Utilities").type(TransactionType.EXPENSE).isCustom(false).build()
            );
            categoryRepository.saveAll(defaults);
            log.info("Default categories created successfully.");
        }
    }
}
