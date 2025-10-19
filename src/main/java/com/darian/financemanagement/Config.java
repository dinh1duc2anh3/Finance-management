package com.darian.financemanagement;

import io.github.cdimascio.dotenv.Dotenv;

public class Config {
    private static final Dotenv dotenv = Dotenv.configure()
            .ignoreIfMissing() // không báo lỗi nếu thiếu file .env
            .load();

    public static final String SPREADSHEET_URL = dotenv.get("SPREADSHEET_URL");
    public static final String APPLICATION_NAME = dotenv.get("APPLICATION_NAME", "Finance Management App");
    public static final String CREDENTIALS_FILE_PATH = dotenv.get("CREDENTIALS_FILE_PATH");
    public static final String GOOGLE_CREDENTIALS_JSON = dotenv.get("GOOGLE_CREDENTIALS");

    public static String extractSpreadsheetId(String sheetUrl){
        if (sheetUrl == null || sheetUrl.isEmpty()) {
            return null;
        }

        // Google Sheets URL thường có dạng:
        // https://docs.google.com/spreadsheets/d/<SPREADSHEET_ID>/edit...
        String pattern = "https://docs\\.google\\.com/spreadsheets/d/([a-zA-Z0-9-_]+)";
        java.util.regex.Pattern regex = java.util.regex.Pattern.compile(pattern);
        java.util.regex.Matcher matcher = regex.matcher(sheetUrl);

        if (matcher.find()) {
            return matcher.group(1);
        }

        return null; // Không tìm thấy ID
    }
}
