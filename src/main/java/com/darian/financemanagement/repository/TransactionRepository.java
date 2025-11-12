package com.darian.financemanagement.repository;

import com.darian.financemanagement.model.Transaction;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface TransactionRepository extends MongoRepository<Transaction, String> {
    List<Transaction> findBySheetConfigId(String sheetConfigId);
    List<Transaction> findBySheetConfigIdOrderByCreatedAtDesc(String sheetConfigId);
    long countBySheetConfigIdAndTransactionIdStartingWith(String sheetConfigId, String prefix);
}
