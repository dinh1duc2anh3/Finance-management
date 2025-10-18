package com.darian.financemanagement.dto;

public class ExpenseRequest {
    private String name;
    private String expense;
    private String date;

    public ExpenseRequest() {
    }

    public ExpenseRequest(String name, String expense, String date) {
        this.name = name;
        this.expense = expense;
        this.date = date;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getExpense() {
        return expense;
    }

    public void setExpense(String expense) {
        this.expense = expense;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return "ExpenseRequest{" +
                "name='" + name + '\'' +
                ", expense='" + expense + '\'' +
                ", date='" + date + '\'' +
                '}';
    }
}
