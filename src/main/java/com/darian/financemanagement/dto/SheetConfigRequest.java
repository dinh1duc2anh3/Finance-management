package com.darian.financemanagement.dto;

public class SheetConfigRequest {
    private String spreadSheetUrl;
    private String spreadsheetName;
    private String sheetName;
    private String range;

    public SheetConfigRequest() {
    }

    public SheetConfigRequest(String spreadSheetUrl, String spreadsheetName, String sheetName, String range) {
        this.spreadSheetUrl = spreadSheetUrl;
        this.spreadsheetName = spreadsheetName;
        this.sheetName = sheetName;
        this.range = range;
    }

    // --- Getters & Setters ---
    public String getSpreadSheetUrl() { return spreadSheetUrl; }
    public void setSpreadSheetUrl(String spreadSheetUrl) { this.spreadSheetUrl = spreadSheetUrl; }
    public String getSpreadsheetName() { return spreadsheetName; }
    public void setSpreadsheetName(String spreadsheetName) { this.spreadsheetName = spreadsheetName; }
    public String getSheetName() { return sheetName; }
    public void setSheetName(String sheetName) { this.sheetName = sheetName; }
    public String getRange() { return range; }
    public void setRange(String range) { this.range = range; }


}
