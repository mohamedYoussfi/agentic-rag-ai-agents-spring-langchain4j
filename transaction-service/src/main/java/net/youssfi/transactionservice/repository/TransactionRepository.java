package net.youssfi.transactionservice.repository;

import net.youssfi.transactionservice.entities.Transaction;
import net.youssfi.transactionservice.entities.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByAccountId(long accountId);
    List<Transaction> findByStatus(TransactionStatus transactionStatus);
}
