package com.finance.manager.config;

import com.finance.manager.entity.Category;
import com.finance.manager.entity.TransactionType;
import com.finance.manager.repository.CategoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DataInitializerTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private DataInitializer dataInitializer;

    @Test
    void run_SeedsDefaultsWhenNoneExist() {
        when(categoryRepository.findByUserIsNull()).thenReturn(Collections.emptyList());

        dataInitializer.run();

        ArgumentCaptor<List<Category>> captor = ArgumentCaptor.forClass(List.class);
        verify(categoryRepository).saveAll(captor.capture());

        List<Category> saved = captor.getValue();
        assertEquals(7, saved.size());
        assertTrue(saved.stream().anyMatch(c -> c.getName().equals("Salary") && c.getType() == TransactionType.INCOME));
        assertTrue(saved.stream().anyMatch(c -> c.getName().equals("Food") && c.getType() == TransactionType.EXPENSE));
        assertTrue(saved.stream().anyMatch(c -> c.getName().equals("Rent")));
        assertTrue(saved.stream().anyMatch(c -> c.getName().equals("Transportation")));
        assertTrue(saved.stream().anyMatch(c -> c.getName().equals("Entertainment")));
        assertTrue(saved.stream().anyMatch(c -> c.getName().equals("Healthcare")));
        assertTrue(saved.stream().anyMatch(c -> c.getName().equals("Utilities")));
        assertTrue(saved.stream().noneMatch(Category::isCustom));
    }

    @Test
    void run_SkipsSeedingWhenDefaultsExist() {
        Category existing = Category.builder().id(1L).name("Salary").type(TransactionType.INCOME).isCustom(false).build();
        when(categoryRepository.findByUserIsNull()).thenReturn(List.of(existing));

        dataInitializer.run();

        verify(categoryRepository, never()).saveAll(org.mockito.ArgumentMatchers.anyList());
    }
}
