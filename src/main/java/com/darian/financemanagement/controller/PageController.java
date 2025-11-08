package com.darian.financemanagement.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping({"/add"})
    public String addPage() {
        return "add";
    }

    @GetMapping({"/transactions"})
    public String transactionsPage() {
        return "transactions";
    }
}
