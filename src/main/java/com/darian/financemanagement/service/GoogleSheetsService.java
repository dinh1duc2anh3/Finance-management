package com.darian.financemanagement.service;

import com.darian.financemanagement.Config;
import com.darian.financemanagement.dto.ExpenseRequest;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.AppendValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.util.FileUtils;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class GoogleSheetsService {


    private Sheets getSheetsService() throws IOException, GeneralSecurityException {
        String credentialsJson = Config.GOOGLE_CREDENTIALS_JSON;
        InputStream in;

        try {
            if (credentialsJson != null && !credentialsJson.isEmpty()){
                System.out.println("Deploy: Using GOOGLE_CREDENTIALS from environment");
                in = new ByteArrayInputStream(credentialsJson.getBytes(StandardCharsets.UTF_8));
            } else  {
                String filePath = Config.CREDENTIALS_FILE_PATH;
                System.out.println("Local: Using local credentials file: " + filePath);
                
                in = FileUtils.getInputStream(filePath);
            }

            var credentials = ServiceAccountCredentials
                    .fromStream(in)
                    .createScoped(Collections.singleton(SheetsScopes.SPREADSHEETS));
            System.out.println("ServiceAccountCredentials created successfully");

            // Đảm bảo APPLICATION_NAME không null
            String appName = Config.APPLICATION_NAME;

            return new Sheets.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    GsonFactory.getDefaultInstance(),
                    new HttpCredentialsAdapter(credentials))
                    .setApplicationName(appName)
                    .build();
        } catch (NullPointerException e) {
            System.err.println("NullPointerException when creating Sheets service: " + e.getMessage());
            e.printStackTrace();
            throw new IOException("Error parsing Google credentials. Please check your credentials JSON file. " , e);
        }
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

        List<Object> rowData = new ArrayList<>();
        rowData.add(request.getDate() != null ? request.getDate() : "");
        rowData.add(request.getTime() != null ? request.getTime() : "");
        rowData.add(request.getTransaction() != null ? request.getTransaction() : "");
        rowData.add(request.getGroup() != null ? request.getGroup() : "");
        rowData.add(request.getSubgroup() != null ? request.getSubgroup() : "");
        rowData.add(request.getCategory() != null ? request.getCategory() : "");
        rowData.add(request.getAmount() != null ? request.getAmount() : "");
        rowData.add(request.getNote() != null ? request.getNote() : "");

        ValueRange appendBody = new ValueRange()
                .setValues(Collections.singletonList(rowData));

        AppendValuesResponse response = service.spreadsheets().values()
                .append(spreadsheetId, range, appendBody)
                .setValueInputOption("USER_ENTERED")
                .execute();

        System.out.println("Data appended successfully: " + response.getUpdates().getUpdatedRange());
    }
}
