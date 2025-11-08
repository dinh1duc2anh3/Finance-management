package com.darian.financemanagement.controller;

import com.darian.financemanagement.Config;
import com.darian.financemanagement.dto.ExpenseRequest;
import com.darian.financemanagement.service.GoogleSheetsService;
import com.darian.financemanagement.service.IdempotencyService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Map;

@RestController
//@RequestMapping("/sheet")
public class SheetController {

    private final GoogleSheetsService sheetsService = new GoogleSheetsService();
    private final IdempotencyService idempotencyService ;


    public SheetController(IdempotencyService idempotencyService) {
        this.idempotencyService = idempotencyService;
    }

    private static final String SPREADSHEET_ID = Config.extractSpreadsheetId(Config.SPREADSHEET_URL);
    private static final String RANGE = "test1!A:H";

    private static final String IDEMPOTENCY_KEY_HEADER = "Idempotency-Key";

    @GetMapping("/read-sheet")
    public List<List<Object>> readSheet() throws Exception {
        return sheetsService.readData(SPREADSHEET_ID, RANGE);
    }

    @PostMapping("/append")
    public ResponseEntity<String> appendRow(
            @RequestBody ExpenseRequest request, 
            @RequestHeader( value = IDEMPOTENCY_KEY_HEADER, required = false) String idempotencyKey) {

        if (idempotencyKey != null && !idempotencyKey.isEmpty()) {
            String cachedResponse = idempotencyService.getCacheResponse(idempotencyKey);
            if (cachedResponse != null) {
                System.out.println("Duplicate request detected. Returning cached response: " + cachedResponse);
                return ResponseEntity.ok("Duplicate request detected. Returning cached response: " + cachedResponse);
            }
        }

        System.out.println("Received request: " + request.toString());

        try {
            sheetsService.appendData(SPREADSHEET_ID, RANGE, request);
            String successMsg = "Row added successfully: " + request.getTransaction() 
                    + " on " + request.getDate() + " " + request.getTime();
            if (idempotencyKey != null && !idempotencyKey.isEmpty()) {
                idempotencyService.cacheResponse(idempotencyKey, successMsg);
            }

            return ResponseEntity.ok(successMsg);
        } catch (IOException e) {
            System.err.println("IOException: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error saving to Google Sheets: " + e.getMessage());
        } catch (GeneralSecurityException e) {
            System.err.println("SecurityException: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Authentication error: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unexpected error: " + e.getMessage());
        }
    }

    @DeleteMapping("/delete-row/{rowIndex}")
    public ResponseEntity<String> deleteRow(@PathVariable int rowIndex) {
        try {
            sheetsService.deleteRow(SPREADSHEET_ID, rowIndex);
            return ResponseEntity.ok("Row " + rowIndex + " deleted successfully");
        } catch (IOException e) {
            System.err.println("IOException: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error deleting row: " + e.getMessage());
        } catch (GeneralSecurityException e) {
            System.err.println("SecurityException: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Authentication error: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unexpected error: " + e.getMessage());
        }
    }

    @DeleteMapping("/delete-rows")
    public ResponseEntity<String> deleteRows(@RequestBody List<Integer> rowIndices) {
        try {
            sheetsService.deleteRows(SPREADSHEET_ID, rowIndices);
            return ResponseEntity.ok(rowIndices.size() + " row(s) deleted successfully");
        } catch (IOException e) {
            System.err.println("IOException: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error deleting rows: " + e.getMessage());
        } catch (GeneralSecurityException e) {
            System.err.println("SecurityException: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Authentication error: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unexpected error: " + e.getMessage());
        }
    }

    @PostMapping("/clone-row")
    public ResponseEntity<String> cloneRow(@RequestBody Map<String, Object> request) {
        try {
            Integer rowIndex = (Integer) request.get("rowIndex");
            if (rowIndex == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("rowIndex is required");
            }
            sheetsService.cloneRow(SPREADSHEET_ID, RANGE, rowIndex);
            return ResponseEntity.ok("Row " + rowIndex + " cloned successfully");
        } catch (IOException e) {
            System.err.println("IOException: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error cloning row: " + e.getMessage());
        } catch (GeneralSecurityException e) {
            System.err.println("SecurityException: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Authentication error: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unexpected error: " + e.getMessage());
        }
    }
}
