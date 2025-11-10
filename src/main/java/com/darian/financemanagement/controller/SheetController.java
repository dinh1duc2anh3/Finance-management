package com.darian.financemanagement.controller;

import com.darian.financemanagement.dto.ExpenseRequest;
import com.darian.financemanagement.model.SheetConfig;
import com.darian.financemanagement.service.GoogleSheetsService;
import com.darian.financemanagement.service.IdempotencyService;
import com.darian.financemanagement.service.SheetConfigService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Map;

@RestController
public class SheetController {

    private final GoogleSheetsService sheetsService = new GoogleSheetsService();
    private final IdempotencyService idempotencyService ;
    private final SheetConfigService sheetConfigService;

    public SheetController(IdempotencyService idempotencyService, SheetConfigService sheetConfigService) {
        this.idempotencyService = idempotencyService;
        this.sheetConfigService = sheetConfigService;
    }

    private static final String IDEMPOTENCY_KEY_HEADER = "Idempotency-Key";

    private SheetConfig resolveConfig(String configId) {
        if (configId == null || configId.isBlank()) {
            throw new IllegalArgumentException("configId is required");
        }
        return sheetConfigService.getConfigById(configId);
    }

    @GetMapping("/read-sheet")
    public ResponseEntity<?> readSheet(@RequestParam String configId) {
        try {
            SheetConfig cfg = resolveConfig(configId);
            List<List<Object>> data = sheetsService.readData(cfg.getSpreadsheetId(), cfg.buildFullRange());
            return ResponseEntity.ok(data);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to read sheet: " + e.getMessage());
        }
    }

    @PostMapping("/append")
    public ResponseEntity<String> appendRow(
            @RequestParam String configId,
            @RequestBody ExpenseRequest request,
            @RequestHeader(value = IDEMPOTENCY_KEY_HEADER, required = false) String idempotencyKey) {

        if (idempotencyKey != null && !idempotencyKey.isEmpty()) {
            String cachedResponse = idempotencyService.getCacheResponse(idempotencyKey);
            if (cachedResponse != null) {
                return ResponseEntity.ok("Duplicate request detected. Returning cached response: " + cachedResponse);
            }
        }

        try {
            SheetConfig cfg = resolveConfig(configId);
            sheetsService.appendData(cfg.getSpreadsheetId(), cfg.buildFullRange(), request);
            String successMsg = "Row added successfully: " + request.getTransaction() +
                    " on " + request.getDate() + " " + request.getTime();
            if (idempotencyKey != null && !idempotencyKey.isEmpty()) {
                idempotencyService.cacheResponse(idempotencyKey, successMsg);
            }
            return ResponseEntity.ok(successMsg);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IOException | GeneralSecurityException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error saving to Google Sheets: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unexpected error: " + e.getMessage());
        }
    }

    @DeleteMapping("/delete-row/{rowIndex}")
    public ResponseEntity<String> deleteRow(@RequestParam String configId, @PathVariable int rowIndex) {
        try {
            SheetConfig cfg = resolveConfig(configId);
            sheetsService.deleteRow(cfg.getSpreadsheetId(), cfg.getSheetName(), rowIndex);
            return ResponseEntity.ok("Row " + rowIndex + " deleted successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IOException | GeneralSecurityException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error deleting row: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unexpected error: " + e.getMessage());
        }
    }

    @DeleteMapping("/delete-rows")
    public ResponseEntity<String> deleteRows(@RequestParam String configId, @RequestBody List<Integer> rowIndices) {
        try {
            SheetConfig cfg = resolveConfig(configId);
            sheetsService.deleteRows(cfg.getSpreadsheetId(), cfg.getSheetName(), rowIndices);
            return ResponseEntity.ok(rowIndices.size() + " row(s) deleted successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IOException | GeneralSecurityException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error deleting rows: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unexpected error: " + e.getMessage());
        }
    }

    @PostMapping("/clone-row")
    public ResponseEntity<String> cloneRow(@RequestParam String configId, @RequestBody Map<String, Object> request) {
        try {
            Integer rowIndex = (Integer) request.get("rowIndex");
            if (rowIndex == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("rowIndex is required");
            }
            SheetConfig cfg = resolveConfig(configId);
            sheetsService.cloneRow(cfg.getSpreadsheetId(), cfg.buildFullRange(), rowIndex);
            return ResponseEntity.ok("Row " + rowIndex + " cloned successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IOException | GeneralSecurityException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error cloning row: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unexpected error: " + e.getMessage());
        }
    }
}
