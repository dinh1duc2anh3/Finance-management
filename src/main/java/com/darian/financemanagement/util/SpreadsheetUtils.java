package com.darian.financemanagement.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SpreadsheetUtils {
    public static String extractSpreadsheetId(String spreadsheetUrl){
        if (spreadsheetUrl == null || spreadsheetUrl.isEmpty()) {
            return null;
        }

        // Google Sheets URL thường có dạng:
        // https://docs.google.com/spreadsheets/d/<SPREADSHEET_ID>/edit...
        String pattern = "https://docs\\.google\\.com/spreadsheets/d/([a-zA-Z0-9-_]+)";
        java.util.regex.Pattern regex = java.util.regex.Pattern.compile(pattern);
        java.util.regex.Matcher matcher = regex.matcher(spreadsheetUrl);

        if (matcher.find()) {
            return matcher.group(1);
        }

        return null; // Không tìm thấy ID
    }

    public static int[] extractMonthYearFromSpreadsheetName(String spreadsheetName) {
        if (spreadsheetName == null || spreadsheetName.isEmpty()) {
            return null;
        }

        // "Chi tiêu 9/2025" → [9, 2025]
        Pattern pattern = Pattern.compile("(\\d+)/(\\d{4})");
        Matcher matcher = pattern.matcher(spreadsheetName);
        if (matcher.find()) {
            int month = Integer.parseInt(matcher.group(1));
            int year = Integer.parseInt(matcher.group(2));
            return new int[]{month, year};
        }
        throw new IllegalArgumentException("Cannot extract month/year from sheet name. Expected format: 'M/YYYY' or 'MM/YYYY'");
    }


}
