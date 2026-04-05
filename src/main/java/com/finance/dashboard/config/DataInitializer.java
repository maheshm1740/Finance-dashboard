package com.finance.dashboard.config;

import com.finance.dashboard.entity.Transaction;
import com.finance.dashboard.entity.User;
import com.finance.dashboard.enums.Role;
import com.finance.dashboard.enums.TransactionType;
import com.finance.dashboard.repository.TransactionRepository;
import com.finance.dashboard.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        seedUsers();
        seedTransactions();
    }

    private void seedUsers() {
        if (userRepository.count() > 0) return;

        User admin = User.builder()
                .username("admin")
                .email("admin@finance.com")
                .password(passwordEncoder.encode("admin123"))
                .role(Role.ADMIN)
                .active(true)
                .build();

        User analyst = User.builder()
                .username("analyst")
                .email("analyst@finance.com")
                .password(passwordEncoder.encode("analyst123"))
                .role(Role.ANALYST)
                .active(true)
                .build();

        User viewer = User.builder()
                .username("viewer")
                .email("viewer@finance.com")
                .password(passwordEncoder.encode("viewer123"))
                .role(Role.VIEWER)
                .active(true)
                .build();

        userRepository.save(admin);
        userRepository.save(analyst);
        userRepository.save(viewer);

        log.info("✅ Seeded default users: admin / analyst / viewer");
    }

    private void seedTransactions() {
        if (transactionRepository.count() > 0) return;

        User admin = userRepository.findByUsername("admin").orElseThrow();

        transactionRepository.save(Transaction.builder()
                .amount(new BigDecimal("50000.00"))
                .type(TransactionType.INCOME)
                .category("Salary")
                .date(LocalDate.now().minusDays(5))
                .notes("Monthly salary")
                .createdBy(admin)
                .deleted(false)
                .build());

        transactionRepository.save(Transaction.builder()
                .amount(new BigDecimal("1200.00"))
                .type(TransactionType.EXPENSE)
                .category("Rent")
                .date(LocalDate.now().minusDays(4))
                .notes("Monthly rent")
                .createdBy(admin)
                .deleted(false)
                .build());

        transactionRepository.save(Transaction.builder()
                .amount(new BigDecimal("3500.00"))
                .type(TransactionType.INCOME)
                .category("Freelance")
                .date(LocalDate.now().minusDays(3))
                .notes("Freelance project payment")
                .createdBy(admin)
                .deleted(false)
                .build());

        transactionRepository.save(Transaction.builder()
                .amount(new BigDecimal("800.00"))
                .type(TransactionType.EXPENSE)
                .category("Groceries")
                .date(LocalDate.now().minusDays(2))
                .notes("Weekly groceries")
                .createdBy(admin)
                .deleted(false)
                .build());

        transactionRepository.save(Transaction.builder()
                .amount(new BigDecimal("500.00"))
                .type(TransactionType.EXPENSE)
                .category("Utilities")
                .date(LocalDate.now().minusDays(1))
                .notes("Electricity and water bill")
                .createdBy(admin)
                .deleted(false)
                .build());

        log.info("✅ Seeded sample transactions");
    }
}