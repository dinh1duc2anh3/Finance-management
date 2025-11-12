package com.darian.financemanagement.service;

import com.darian.financemanagement.Config;
import com.darian.financemanagement.dto.ExpenseRequest;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.AppendValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetRequest;
import com.google.api.services.sheets.v4.model.Request;
import com.google.api.services.sheets.v4.model.DeleteDimensionRequest;
import com.google.api.services.sheets.v4.model.DimensionRange;
import com.google.api.services.sheets.v4.model.Spreadsheet;
import com.google.api.services.sheets.v4.model.Sheet;
import com.google.api.services.sheets.v4.model.SheetProperties;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.darian.financemanagement.util.FileUtils;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
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

    private Integer resolveSheetIdByName(Sheets service, String spreadsheetId, String sheetName) throws IOException {
        Spreadsheet spreadsheet = service.spreadsheets().get(spreadsheetId).execute();
        if (spreadsheet.getSheets() == null) return null;
        for (Sheet s : spreadsheet.getSheets()) {
            SheetProperties props = s.getProperties();
            if (props != null && sheetName.equals(props.getTitle())) {
                return props.getSheetId();
            }
        }
        return null;
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

    public void deleteRow(String spreadsheetId, String sheetName, int rowIndex)
            throws IOException, GeneralSecurityException {
        Sheets service = getSheetsService();

        Integer sheetId = resolveSheetIdByName(service, spreadsheetId, sheetName);
        if (sheetId == null) {
            throw new IOException("Sheet with name '" + sheetName + "' not found");
        }
        int zeroBasedRowIndex = rowIndex - 1;

        DeleteDimensionRequest deleteRequest = new DeleteDimensionRequest()
                .setRange(new DimensionRange()
                        .setSheetId(sheetId)
                        .setDimension("ROWS")
                        .setStartIndex(zeroBasedRowIndex)
                        .setEndIndex(zeroBasedRowIndex + 1));

        Request request = new Request().setDeleteDimension(deleteRequest);
        BatchUpdateSpreadsheetRequest batchRequest = new BatchUpdateSpreadsheetRequest()
                .setRequests(Collections.singletonList(request));

        service.spreadsheets().batchUpdate(spreadsheetId, batchRequest).execute();
        System.out.println("Row " + rowIndex + " deleted successfully");
    }

    public void deleteRows(String spreadsheetId, String sheetName, List<Integer> rowIndices)
            throws IOException, GeneralSecurityException {
        Sheets service = getSheetsService();

        Integer sheetId = resolveSheetIdByName(service, spreadsheetId, sheetName);
        if (sheetId == null) {
            throw new IOException("Sheet with name '" + sheetName + "' not found");
        }

        List<Integer> sortedIndices = new ArrayList<>(rowIndices);
        sortedIndices.sort((a, b) -> b.compareTo(a));

        List<Request> requests = new ArrayList<>();
        for (Integer rowIndex : sortedIndices) {
            int zeroBasedRowIndex = rowIndex - 1;
            DeleteDimensionRequest deleteRequest = new DeleteDimensionRequest()
                    .setRange(new DimensionRange()
                            .setSheetId(sheetId)
                            .setDimension("ROWS")
                            .setStartIndex(zeroBasedRowIndex)
                            .setEndIndex(zeroBasedRowIndex + 1));
            requests.add(new Request().setDeleteDimension(deleteRequest));
        }

        BatchUpdateSpreadsheetRequest batchRequest = new BatchUpdateSpreadsheetRequest()
                .setRequests(requests);

        service.spreadsheets().batchUpdate(spreadsheetId, batchRequest).execute();
        System.out.println("Rows deleted successfully: " + rowIndices);
    }

    public void cloneRow(String spreadsheetId, String range, int rowIndex)
            throws IOException, GeneralSecurityException {
        Sheets service = getSheetsService();
        String sheetName = range.split("!")[0];
        String readRange = sheetName + "!A" + rowIndex + ":H" + rowIndex;
        ValueRange response = service.spreadsheets().values()
                .get(spreadsheetId, readRange)
                .execute();

        List<List<Object>> values = response.getValues();
        if (values == null || values.isEmpty()) {
            throw new IOException("Row " + rowIndex + " not found or empty");
        }

        List<Object> rowData = values.get(0);
        // Ensure we have 8 columns (A-H)
        while (rowData.size() < 8) {
            rowData.add("");
        }

        ValueRange appendBody = new ValueRange()
                .setValues(Collections.singletonList(rowData));

        AppendValuesResponse appendResponse = service.spreadsheets().values()
                .append(spreadsheetId, range, appendBody)
                .setValueInputOption("USER_ENTERED")
                .execute();

        System.out.println("Row " + rowIndex + " cloned successfully: " + appendResponse.getUpdates().getUpdatedRange());
    }
}
