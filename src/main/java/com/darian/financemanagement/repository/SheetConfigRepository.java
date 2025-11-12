package com.darian.financemanagement.repository;

import com.darian.financemanagement.model.SheetConfig;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface SheetConfigRepository extends MongoRepository<SheetConfig, String> {
    Optional<SheetConfig> findBySpreadsheetIdAndSheetName(String spreadsheetId, String sheetName);
    Optional<SheetConfig> findByUserId(String userId);
    Optional<SheetConfig> findBySpreadsheetId(String spreadsheetId); // Added to check uniqueness constraint pre-save
    // For listing on home page
    List<SheetConfig> findAllByUserIdOrderByYearDescMonthDesc(String userId);
}
