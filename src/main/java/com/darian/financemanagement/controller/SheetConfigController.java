package com.darian.financemanagement.controller;
import com.darian.financemanagement.Config;
import com.darian.financemanagement.dto.SheetConfigRequest;
import com.darian.financemanagement.model.SheetConfig;
import com.darian.financemanagement.service.SheetConfigService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@Controller
public class SheetConfigController {

    private final SheetConfigService sheetConfigService;

    public SheetConfigController(SheetConfigService sheetConfigService) {
        this.sheetConfigService = sheetConfigService;
    }

    // New page for adding a sheet config
    @GetMapping("/sheet-configs/new")
    public String newSheetConfigPage() {
        return "sheet-config-new"; // form template
    }

    @PostMapping("/setup-sheet")
    @ResponseBody
    public ResponseEntity<?> setupSheet(@RequestBody SheetConfigRequest request) {
        Map<String, Object> response = new HashMap<>();
        try {
            String userId = "darian"; // In real application, get from auth context
            SheetConfig config = sheetConfigService.validateAndSave(request, userId);
            response.put("success", true);
            response.put("message", "Sheet configured successfully");
            response.put("configId", config.getId());
            response.put("displayPeriod", config.getMonth() + "/" + config.getYear());
            response.put("serviceAccount", Config.SERVICE_ACCOUNT_EMAIL);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "Failed to connect: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}
