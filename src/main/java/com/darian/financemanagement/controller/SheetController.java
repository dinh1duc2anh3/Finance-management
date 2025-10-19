package com.darian.financemanagement.controller;

import com.darian.financemanagement.Config;
import com.darian.financemanagement.dto.ExpenseRequest;
import com.darian.financemanagement.service.GoogleSheetsService;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

@RestController
//@RequestMapping("/sheet")
public class SheetController {

    private final GoogleSheetsService sheetsService = new GoogleSheetsService();

    private static final String SPREADSHEET_ID = Config.extractSpreadsheetId(Config.SPREADSHEET_URL);
    private static final String RANGE = "test1!A:H";

    @GetMapping("/read-sheet")
    public List<List<Object>> readSheet() throws Exception {
        return sheetsService.readData(SPREADSHEET_ID, RANGE);
    }

    @PostMapping("/append")
    public String appendRow(@RequestBody ExpenseRequest request)
            throws IOException, GeneralSecurityException {
        sheetsService.appendData(SPREADSHEET_ID, RANGE, request);

        return "✅ Row added successfully: " + request.getTransaction()  + " on " + request.getDate()+ " "+ request.getTime();
    }
}
