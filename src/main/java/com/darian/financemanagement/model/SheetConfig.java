package com.darian.financemanagement.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDateTime;

@Document(collection = "sheet_configs")
public class SheetConfig {
    @Id
    private String id;

    private String userId; // ID of the user who owns this sheet config : default to "darian"
    @Indexed(unique = true) // ensure spreadsheetId is unique across documents
    private String spreadsheetId; // extracted from spreadsheetUrl
    private String spreadsheetName; // e.g., "Chi tiêu 9/2025"
    private String sheetName; // default to "Transactions", a tab in the spreadsheet
    private String range;

    private int month;
    private int year;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public SheetConfig() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }


    // --- Getters & Setters ---
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getSpreadsheetId() { return spreadsheetId; }
    public void setSpreadsheetId(String spreadsheetId) { this.spreadsheetId = spreadsheetId; }

    public String getSpreadsheetName() { return spreadsheetName; }
    public void setSpreadsheetName(String spreadsheetName) { this.spreadsheetName = spreadsheetName; }

    public String getSheetName() { return sheetName; }
    public void setSheetName(String sheetName) { this.sheetName = sheetName; }

    public String getRange() { return range; }
    public void setRange(String range) { this.range = range; }

    public int getMonth() { return month; }
    public void setMonth(int month) { this.month = month; }

    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String buildFullRange() {
        return this.sheetName + "!" + this.range;
    }
}
