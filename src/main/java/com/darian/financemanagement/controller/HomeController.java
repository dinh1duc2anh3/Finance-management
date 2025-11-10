package com.darian.financemanagement.controller;
import com.darian.financemanagement.dto.SheetConfigRequest;
import com.darian.financemanagement.model.SheetConfig;
import com.darian.financemanagement.service.SheetConfigService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class HomeController {

    private final SheetConfigService sheetConfigService;

    public HomeController(SheetConfigService sheetConfigService) {
        this.sheetConfigService = sheetConfigService;
    }

    // Root page now shows list of existing sheet configs
    @GetMapping({"/", "/home"})
    public String index(Model model) {
        String userId = "darian"; // replace with authenticated user id when available
        List<SheetConfig> configs = sheetConfigService.listConfigsForUser(userId);
        model.addAttribute("configs", configs);
        model.addAttribute("userId", userId);
        return "index"; // index.html shows list
    }
}
