package com.darian.financemanagement.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TransactionController {

    @GetMapping({"/add"})
    public String addPage() {
        return "add";
    }

    @GetMapping({"/transactions"})
    public String transactionsPage() {
        return "transactions";
    }
}
