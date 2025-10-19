package com.darian.financemanagement.service;

import com.darian.financemanagement.Config;
import com.darian.financemanagement.dto.ExpenseRequest;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.AppendValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;


public class GoogleSheetsService {


    private Sheets getSheetsService() throws IOException, GeneralSecurityException {
        String credentialsJson = Config.GOOGLE_CREDENTIALS_JSON;
        InputStream in;

        if (credentialsJson != null && !credentialsJson.isEmpty()){
            // Deploy: đọc từ biến môi trường GOOGLE_CREDENTIALS
            System.out.println("Using GOOGLE_CREDENTIALS from environment");
            in = new ByteArrayInputStream(credentialsJson.getBytes(StandardCharsets.UTF_8));
        } else  {
            // Local: đọc từ file thật (path trong .env)
            System.out.println("Using local credentials file: " + Config.CREDENTIALS_FILE_PATH);
            in = GoogleSheetsService.class.getResourceAsStream(Config.CREDENTIALS_FILE_PATH);
            if (in == null) {
                throw new IOException("Không tìm thấy file credentials: " + Config.CREDENTIALS_FILE_PATH);
            }
        }

        var credentials = ServiceAccountCredentials
                .fromStream(in)
                .createScoped(Collections.singleton(SheetsScopes.SPREADSHEETS));

        return new Sheets.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(),
                new HttpCredentialsAdapter(credentials))
                .setApplicationName(Config.APPLICATION_NAME)
                .build();
    }

    public List<List<Object>> readData(String spreadsheetId, String range)
            throws IOException, GeneralSecurityException {
        Sheets service = getSheetsService();
        ValueRange response = service.spreadsheets().values()
                .get(spreadsheetId, range)
                .execute();
        return response.getValues();
    }

    public void appendData(String spreadsheetId, String range, ExpenseRequest request)
            throws IOException, GeneralSecurityException {
        Sheets service = getSheetsService();

        List<Object> rowData = List.of(
                request.getDate(),
                request.getTime(),
                request.getTransaction(),
                request.getGroup(),
                request.getSubgroup(),
                request.getCategory(),
                request.getAmount(),
                request.getNote()
        );

        ValueRange appendBody = new ValueRange()
                .setValues(Collections.singletonList(rowData));

        AppendValuesResponse response = service.spreadsheets().values()
                .append(spreadsheetId, range, appendBody)
                .setValueInputOption("USER_ENTERED")
                .execute();

        System.out.println("✅ Data appended successfully: " + response.getUpdates().getUpdatedRange());
    }
}
