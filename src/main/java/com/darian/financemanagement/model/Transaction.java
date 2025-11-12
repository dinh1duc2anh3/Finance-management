package com.darian.financemanagement.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "transactions")
public class Transaction {
    @Id
    private String id;

    private String sheetConfigId;
    private String transactionId; // Format: YYYYMMXXX (e.g., 202509001)
    private String date; // Format: YYYY-MM-DD
    private String time; // Format: HH:MM
    private String description;
    private String group;
    private String subgroup;
    private String category;
    private double amount;
    private String note;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Transaction() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getSheetConfigId() { return sheetConfigId; }
    public void setSheetConfigId(String sheetConfigId) { this.sheetConfigId = sheetConfigId; }

    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }



    public String getNotes() { return note; }
    public void setNotes(String note) { this.note = note; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }


}
