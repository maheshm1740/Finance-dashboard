package com.finance.dashboard.repository;

import com.finance.dashboard.entity.Transaction;
import com.finance.dashboard.enums.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    // Exclude soft-deleted records by default
    Page<Transaction> findByDeletedFalse(Pageable pageable);

    Optional<Transaction> findByIdAndDeletedFalse(Long id);

    // Filter by type
    Page<Transaction> findByTypeAndDeletedFalse(TransactionType type, Pageable pageable);

    // Filter by category
    Page<Transaction> findByCategoryIgnoreCaseAndDeletedFalse(String category, Pageable pageable);

    // Filter by date range
    Page<Transaction> findByDateBetweenAndDeletedFalse(LocalDate startDate, LocalDate endDate, Pageable pageable);

    // Filter by type and date range
    Page<Transaction> findByTypeAndDateBetweenAndDeletedFalse(
            TransactionType type, LocalDate startDate, LocalDate endDate, Pageable pageable);

    // Filter by category and date range
    Page<Transaction> findByCategoryIgnoreCaseAndDateBetweenAndDeletedFalse(
            String category, LocalDate startDate, LocalDate endDate, Pageable pageable);

    // Dashboard: total by type
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.type = :type AND t.deleted = false")
    BigDecimal sumByType(@Param("type") TransactionType type);

    // Dashboard: category wise totals
    @Query("SELECT t.category, SUM(t.amount) FROM Transaction t WHERE t.deleted = false GROUP BY t.category")
    List<Object[]> sumGroupedByCategory();

    // Dashboard: monthly trends
    @Query("SELECT MONTH(t.date), YEAR(t.date), t.type, SUM(t.amount) " +
            "FROM Transaction t WHERE t.deleted = false " +
            "GROUP BY YEAR(t.date), MONTH(t.date), t.type " +
            "ORDER BY YEAR(t.date), MONTH(t.date)")
    List<Object[]> monthlyTrends();

    // Recent transactions
    List<Transaction> findTop5ByDeletedFalseOrderByCreatedAtDesc();
}