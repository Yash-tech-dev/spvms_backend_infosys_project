package com.example.demo.Controller;

import com.example.demo.Service.AnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
    @RequestMapping("/api/reports")
    public class AnalyticsController {

        @Autowired
        private AnalyticsService analyticsService;

        @GetMapping("/spending")
        public ResponseEntity<?> getSpending() {
            // Method calls stored procedure for monthly cost analysis
            return ResponseEntity.ok(analyticsService.getMonthlySpending());

        }

        @GetMapping("/vendor-performance")
        public ResponseEntity<List<Map<String, Object>>> getVendorPerformance() {
            // this helps to define the best vendor among all of them
            List<Map<String, Object>> report = analyticsService.getVendorPerformance();
            return ResponseEntity.ok(report);
        }

        @GetMapping("/fulfillment")
        public ResponseEntity<?> getFulfillment() {
            // Returns percentage of items successfully delivered vs ordered
            return ResponseEntity.ok(analyticsService.getFulfillmentRate());
        }

        @GetMapping("/approvals")
        public ResponseEntity<?> getApprovalTimeline() {
            // Measures internal team efficiency in approving requests
            return ResponseEntity.ok(analyticsService.getApprovalTimeline());
        }

    @GetMapping("/dashboard-summary")
    public ResponseEntity<Map<String, Object>> getDashboard() {
            // Provides the combined result of all the stored procedures
        return ResponseEntity.ok(analyticsService.getFullDashboard());
    }


    }

