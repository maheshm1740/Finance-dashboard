package com.finance.dashboard.service;

import com.finance.dashboard.dto.request.TransactionRequest;
import com.finance.dashboard.dto.response.TransactionResponse;
import com.finance.dashboard.entity.Transaction;
import com.finance.dashboard.entity.User;
import com.finance.dashboard.enums.TransactionType;
import com.finance.dashboard.exception.ResourceNotFoundException;
import com.finance.dashboard.repository.TransactionRepository;
import com.finance.dashboard.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    public TransactionResponse createTransaction(TransactionRequest request) {
        User currentUser = getCurrentUser();

        Transaction transaction = Transaction.builder()
                .amount(request.getAmount())
                .type(request.getType())
                .category(request.getCategory())
                .date(request.getDate())
                .notes(request.getNotes())
                .createdBy(currentUser)
                .deleted(false)
                .build();

        return mapToResponse(transactionRepository.save(transaction));
    }

    public Page<TransactionResponse> getAllTransactions(
            TransactionType type,
            String category,
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable) {

        // Apply filters based on provided parameters
        if (type != null && startDate != null && endDate != null) {
            return transactionRepository
                    .findByTypeAndDateBetweenAndDeletedFalse(type, startDate, endDate, pageable)
                    .map(this::mapToResponse);
        }

        if (category != null && startDate != null && endDate != null) {
            return transactionRepository
                    .findByCategoryIgnoreCaseAndDateBetweenAndDeletedFalse(category, startDate, endDate, pageable)
                    .map(this::mapToResponse);
        }

        if (type != null) {
            return transactionRepository
                    .findByTypeAndDeletedFalse(type, pageable)
                    .map(this::mapToResponse);
        }

        if (category != null) {
            return transactionRepository
                    .findByCategoryIgnoreCaseAndDeletedFalse(category, pageable)
                    .map(this::mapToResponse);
        }

        if (startDate != null && endDate != null) {
            return transactionRepository
                    .findByDateBetweenAndDeletedFalse(startDate, endDate, pageable)
                    .map(this::mapToResponse);
        }

        return transactionRepository
                .findByDeletedFalse(pageable)
                .map(this::mapToResponse);
    }

    public TransactionResponse getTransactionById(Long id) {
        return mapToResponse(findTransactionById(id));
    }

    public TransactionResponse updateTransaction(Long id, TransactionRequest request) {
        Transaction transaction = findTransactionById(id);

        transaction.setAmount(request.getAmount());
        transaction.setType(request.getType());
        transaction.setCategory(request.getCategory());
        transaction.setDate(request.getDate());
        transaction.setNotes(request.getNotes());

        return mapToResponse(transactionRepository.save(transaction));
    }

    public void deleteTransaction(Long id) {
        Transaction transaction = findTransactionById(id);
        // Soft delete
        transaction.setDeleted(true);
        transactionRepository.save(transaction);
    }

    private Transaction findTransactionById(Long id) {
        return transactionRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", id));
    }

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
    }

    public TransactionResponse mapToResponse(Transaction transaction) {
        return TransactionResponse.builder()
                .id(transaction.getId())
                .amount(transaction.getAmount())
                .type(transaction.getType())
                .category(transaction.getCategory())
                .date(transaction.getDate())
                .notes(transaction.getNotes())
                .createdBy(transaction.getCreatedBy().getUsername())
                .createdAt(transaction.getCreatedAt())
                .updatedAt(transaction.getUpdatedAt())
                .build();
    }
}