package com.darian.financemanagement.dto;

public class ExpenseRequest {
    private String date;
    private String time;
    private String transaction;
    private String group;
    private String subgroup;
    private String category;
    private String amount;
    private String note;

    public ExpenseRequest() {
    }

    public ExpenseRequest(String date, String time, String transaction,
                          String group, String subgroup, String category,
                          String amount, String note) {
        this.date = date;
        this.time = time;
        this.transaction = transaction;
        this.group = group;
        this.subgroup = subgroup;
        this.category = category;
        this.amount = amount;
        this.note = note;
    }

    // --- Getters & Setters ---
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public String getTransaction() { return transaction; }
    public void setTransaction(String transaction) { this.transaction = transaction; }

    public String getGroup() { return group; }
    public void setGroup(String group) { this.group = group; }

    public String getSubgroup() { return subgroup; }
    public void setSubgroup(String subgroup) { this.subgroup = subgroup; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getAmount() { return amount; }
    public void setAmount(String amount) { this.amount = amount; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    @Override
    public String toString() {
        return "ExpenseRequest{" +
                "date='" + date + '\'' +
                ", time='" + time + '\'' +
                ", transaction='" + transaction + '\'' +
                ", group='" + group + '\'' +
                ", subgroup='" + subgroup + '\'' +
                ", category='" + category + '\'' +
                ", amount='" + amount + '\'' +
                ", note='" + note + '\'' +
                '}';
    }
}
