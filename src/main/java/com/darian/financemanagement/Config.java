package com.darian.financemanagement;

import io.github.cdimascio.dotenv.Dotenv;

public class Config {
    private static final Dotenv dotenv = Dotenv.configure()
            .ignoreIfMissing() // không báo lỗi nếu thiếu file .env
            .load();

    public static final String SPREADSHEET_URL = dotenv.get("SPREADSHEET_URL");
    public static final String APPLICATION_NAME = dotenv.get("APPLICATION_NAME", "Finance Management App");
    public static final String CREDENTIALS_FILE_PATH = dotenv.get("CREDENTIALS_FILE_PATH", "credentials/sheet-key.json");
    public static final String GOOGLE_CREDENTIALS_JSON = dotenv.get("GOOGLE_CREDENTIALS", "");
    public static final String SERVICE_ACCOUNT_EMAIL = "sheet-writer@finance-management-475508.iam.gserviceaccount.com";
}
