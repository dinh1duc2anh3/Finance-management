package com.darian.financemanagement.service;

import com.darian.financemanagement.dto.SheetConfigRequest;
import com.darian.financemanagement.model.SheetConfig;
import com.darian.financemanagement.repository.SheetConfigRepository;
import com.darian.financemanagement.util.SpreadsheetUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SheetConfigService {
    private final GoogleSheetsService googleSheetsService;
    private final SheetConfigRepository sheetConfigRepository;

    public SheetConfigService(GoogleSheetsService googleSheetsService, SheetConfigRepository sheetConfigRepository) {
        this.googleSheetsService = googleSheetsService;
        this.sheetConfigRepository = sheetConfigRepository;
    }

    public SheetConfig validateAndSave(SheetConfigRequest request, String userId) {
        String spreadsheetId = SpreadsheetUtils.extractSpreadsheetId(request.getSpreadSheetUrl());

        // Pre-check uniqueness to give a friendly error before hitting DB unique index
        sheetConfigRepository.findBySpreadsheetId(spreadsheetId).ifPresent(existing -> {
            throw new IllegalArgumentException("A configuration for this spreadsheet already exists (ID: " + existing.getId() + ")");
        });

        String sheetName = request.getSheetName();
        String range =  request.getRange();
        try {
            List<List<Object>> testRead =  googleSheetsService.readData(spreadsheetId,sheetName + "!" +range);
            System.out.println("Connection Successful: Test read data: " + testRead);
        } catch (Exception e) {
            // Log the original exception to help debugging and include the message in the thrown error
            e.printStackTrace();
            throw new IllegalArgumentException("Failed to connect to the specified Google Sheet.\n" +
                    "Make sure you have granted edit permission for this account \n" +
                    "If still error though permission has been granted, please check the URL, sheet name, and range.\n" +
                    "Underlying error: " + e.getMessage());
        }

        String spreadsheetName = request.getSpreadsheetName();
        int[] monthYear = SpreadsheetUtils.extractMonthYearFromSpreadsheetName(spreadsheetName);

        SheetConfig config = new SheetConfig();
        config.setUserId(userId);
        config.setSpreadsheetId(spreadsheetId);
        config.setSpreadsheetName(spreadsheetName); // newly added
        config.setSheetName(sheetName);
        config.setRange(range);
        config.setMonth(monthYear[0]);
        config.setYear(monthYear[1]);
        config.setUpdatedAt(LocalDateTime.now());

        try {
            return sheetConfigRepository.save(config);
        } catch (DuplicateKeyException dke) {
            throw new IllegalArgumentException("Spreadsheet ID already exists in another configuration.");
        }
    }

    public SheetConfig getConfigById(String id) {
        return sheetConfigRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Sheet config not found"));
    }

    public List<SheetConfig> listConfigsForUser(String userId) {
        return sheetConfigRepository.findAllByUserIdOrderByYearDescMonthDesc(userId);
    }
}
