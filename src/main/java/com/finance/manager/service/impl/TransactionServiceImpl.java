package com.finance.manager.service.impl;

import com.finance.manager.dto.request.TransactionRequest;
import com.finance.manager.dto.request.UpdateTransactionRequest;
import com.finance.manager.dto.response.TransactionResponse;
import com.finance.manager.entity.Category;
import com.finance.manager.entity.Transaction;
import com.finance.manager.entity.TransactionType;
import com.finance.manager.entity.User;
import com.finance.manager.exception.ResourceNotFoundException;
import com.finance.manager.exception.ValidationException;
import com.finance.manager.repository.CategoryRepository;
import com.finance.manager.repository.TransactionRepository;
import com.finance.manager.repository.TransactionSpecifications;
import com.finance.manager.repository.UserRepository;
import com.finance.manager.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * {@inheritDoc}
 */
@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public TransactionResponse createTransaction(String username, TransactionRequest request) {
        User user = getUser(username);

        if (request.getDate().isAfter(LocalDate.now())) {
            throw new ValidationException("Transaction date cannot be in the future");
        }

        Category category = resolveCategory(request.getCategory(), user);

        Transaction transaction = Transaction.builder()
                .amount(request.getAmount())
                .date(request.getDate())
                .category(category)
                .description(request.getDescription())
                .user(user)
                .build();

        Transaction saved = transactionRepository.save(transaction);
        return toResponse(saved);
    }

    @Override
    public List<TransactionResponse> getTransactions(String username, LocalDate startDate, LocalDate endDate, Long categoryId, TransactionType type) {
        User user = getUser(username);

        Category category = null;
        if (categoryId != null) {
            category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + categoryId));
        }

        Specification<Transaction> spec = Specification
                .where(TransactionSpecifications.forUser(user))
                .and(TransactionSpecifications.dateBetween(startDate, endDate))
                .and(TransactionSpecifications.hasCategory(category))
                .and(TransactionSpecifications.hasType(type));

        List<Transaction> transactions = transactionRepository.findAll(spec, Sort.by(Sort.Direction.DESC, "date"));

        return transactions.stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public TransactionResponse updateTransaction(String username, Long id, UpdateTransactionRequest request) {
        User user = getUser(username);

        Transaction transaction = transactionRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found: " + id));

        if (request.getAmount() != null) {
            transaction.setAmount(request.getAmount());
        }
        if (request.getCategory() != null) {
            Category category = resolveCategory(request.getCategory(), user);
            transaction.setCategory(category);
        }
        if (request.getDescription() != null) {
            transaction.setDescription(request.getDescription());
        }

        Transaction updated = transactionRepository.save(transaction);
        return toResponse(updated);
    }

    @Override
    @Transactional
    public Map<String, String> deleteTransaction(String username, Long id) {
        User user = getUser(username);

        Transaction transaction = transactionRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found: " + id));

        transactionRepository.delete(transaction);
        return Map.of("message", "Transaction deleted successfully");
    }

    private Category resolveCategory(String categoryName, User user) {
        return categoryRepository.findByNameAndUserOrDefault(categoryName, user)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + categoryName));
    }

    private User getUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private TransactionResponse toResponse(Transaction t) {
        return TransactionResponse.builder()
                .id(t.getId())
                .amount(t.getAmount())
                .date(t.getDate())
                .category(t.getCategory().getName())
                .description(t.getDescription())
                .type(t.getCategory().getType())
                .build();
    }
}
