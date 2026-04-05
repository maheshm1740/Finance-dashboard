package com.finance.dashboard.service;

import com.finance.dashboard.dto.response.DashboardResponse;
import com.finance.dashboard.dto.response.TransactionResponse;
import com.finance.dashboard.enums.TransactionType;
import com.finance.dashboard.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final TransactionRepository transactionRepository;
    private final TransactionService transactionService;

    public DashboardResponse getSummary() {
        BigDecimal totalIncome = transactionRepository.sumByType(TransactionType.INCOME);
        BigDecimal totalExpenses = transactionRepository.sumByType(TransactionType.EXPENSE);
        BigDecimal netBalance = totalIncome.subtract(totalExpenses);

        Map<String, BigDecimal> categoryWiseTotals = getCategoryWiseTotals();

        List<TransactionResponse> recentTransactions = transactionRepository
                .findTop5ByDeletedFalseOrderByCreatedAtDesc()
                .stream()
                .map(transactionService::mapToResponse)
                .collect(Collectors.toList());

        List<DashboardResponse.MonthlyTrend> monthlyTrends = getMonthlyTrends();

        return DashboardResponse.builder()
                .totalIncome(totalIncome)
                .totalExpenses(totalExpenses)
                .netBalance(netBalance)
                .categoryWiseTotals(categoryWiseTotals)
                .recentTransactions(recentTransactions)
                .monthlyTrends(monthlyTrends)
                .build();
    }

    private Map<String, BigDecimal> getCategoryWiseTotals() {
        Map<String, BigDecimal> totals = new HashMap<>();
        transactionRepository.sumGroupedByCategory()
                .forEach(row -> totals.put(
                        (String) row[0],
                        (BigDecimal) row[1]
                ));
        return totals;
    }

    private List<DashboardResponse.MonthlyTrend> getMonthlyTrends() {
        List<Object[]> raw = transactionRepository.monthlyTrends();

        // Group by year+month and build MonthlyTrend objects
        Map<String, DashboardResponse.MonthlyTrend> trendMap = new HashMap<>();

        for (Object[] row : raw) {
            int month = ((Number) row[0]).intValue();
            int year = ((Number) row[1]).intValue();
            TransactionType type = TransactionType.valueOf(row[2].toString());
            BigDecimal amount = (BigDecimal) row[3];

            String key = year + "-" + month;
            DashboardResponse.MonthlyTrend trend = trendMap.getOrDefault(key,
                    DashboardResponse.MonthlyTrend.builder()
                            .year(year)
                            .month(month)
                            .totalIncome(BigDecimal.ZERO)
                            .totalExpenses(BigDecimal.ZERO)
                            .build());

            if (type == TransactionType.INCOME) {
                trend.setTotalIncome(amount);
            } else {
                trend.setTotalExpenses(amount);
            }

            trendMap.put(key, trend);
        }

        return trendMap.values().stream()
                .sorted((a, b) -> {
                    if (a.getYear() != b.getYear()) return a.getYear() - b.getYear();
                    return a.getMonth() - b.getMonth();
                })
                .collect(Collectors.toList());
    }
}