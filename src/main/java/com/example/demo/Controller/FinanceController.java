package com.example.demo.Controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/finance")
public class FinanceController {

    @GetMapping("/reports")
    public String getReports() {
        return "Finance Module: Access Granted. Displaying financial reports...";
    }
}