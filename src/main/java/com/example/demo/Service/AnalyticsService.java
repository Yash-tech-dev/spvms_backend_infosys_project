package com.example.demo.Service;

import com.example.demo.Repository.PORepository;
import com.example.demo.Repository.PRRepository;
import com.example.demo.Repository.UserRepository;
import com.example.demo.models.POStatus;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service class responsible for aggregating business intelligence and analytics.
 * It interacts with various repositories to generate reports on spending,
 * performance, and fulfillment.
 */
@Service
public class AnalyticsService {

    @Autowired
    private PORepository poRepository;

    @Autowired
    private PRRepository prRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Method: getMonthlySpending
     * Working:
     * 1. Database se Stored Procedure call karta hai.
     * 2. Result ko Map mein convert karta hai (Month -> Amount).
     * @return A list of maps containing month names and spending amounts.
     */
    @Transactional
    public List<Map<String, Object>> getMonthlySpending() {
        List<Object[]> results = poRepository.getMonthlySpendingReport();
        return results.stream().map(row -> {
            Map<String, Object> map = new HashMap<>();
            map.put("month", row[0]);
            map.put("amount", row[1]);
            return map;
        }).collect(Collectors.toList());
    }

    /**
     * Calculates the order fulfillment rate by comparing ordered vs delivered quantities.
     * Working: Calls a stored procedure and maps the raw database rows to a readable JSON-like structure.
     * @return List of maps with item details and their percentage of fulfillment.
     */
    @Transactional
    public List<Map<String, Object>> getFulfillmentRate() {
        List<Object[]> results = poRepository.getFulfillmentReport();
        return results.stream().map(row -> {
            Map<String, Object> map = new HashMap<>();
            map.put("itemName", row[1]);
            map.put("ordered", row[2]);
            map.put("delivered", row[3]);
            map.put("fulfillmentRate", row[4] + "%");
            return map;
        }).collect(Collectors.toList());
    }

    /**
     * Retrieves the timeline data for Purchase Request approvals.
     * Working: Calls the 'GetPRApprovalTimeline' native procedure and handles potential exceptions
     * to avoid breaking the dashboard view.
     * @return List of maps containing requester names and processing duration.
     */
    public List<Map<String, Object>> getApprovalTimeline() {
        try {
            // Calling the repository method linked to the procedure
            List<Object[]> results = poRepository.getPRApprovalReport();
            List<Map<String, Object>> response = new ArrayList<>();

            for (Object[] row : results) {
                Map<String, Object> map = new HashMap<>();
                // row[0] = id, row[1] = username, row[2] = status, row[3] = days
                map.put("id", row[0]);
                map.put("requesterName", row[1]);
                map.put("status", row[2]);
                map.put("daysInProcess", row[3]);
                response.add(map);
            }
            return response;
        } catch (Exception e) {
            // Log error to prevent 403 redirect
            System.err.println("Dashboard Error: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Generates a performance report for all vendors.
     * Working: Fetches order counts, damage incidents, and delivery speed data via a database procedure.
     * @return List of maps detailing vendor reliability metrics.
     */
    @Transactional
    public List<Map<String, Object>> getVendorPerformance() {
        // Repository method ko call kar rahe hain jo 'GetVendorPerformance' procedure chala raha hai
        List<Object[]> results = poRepository.getVendorPerformanceReport();

        return results.stream().map(row -> {
            Map<String, Object> map = new HashMap<>();
            map.put("vendorName", row[0]);
            map.put("totalOrders", row[1]);
            map.put("damagedIncidents", row[2]);
            map.put("avgDeliveryDays", row[3]);

            // Bonus: Quality Score calculation (Optional logic)
            // Agar total orders 10 hain aur 2 damaged hain, toh quality 80% hogi
            return map;
        }).collect(Collectors.toList());
    }

    @Transactional
    /**
     * Aggregates all key performance indicators (KPIs) into a single dashboard object.
     * Working: Combines user counts, active order counts, and all report methods into
     * a master Map for the frontend.
     * @return A master map containing all dashboard statistics and lists.
     */
    public Map<String, Object> getFullDashboard() {
        Map<String, Object> data = new HashMap<>();

        try {
            // 1. Total Vendors (Pehle se working hai)
            data.put("totalVendors", userRepository.countByRole("ROLE_VENDOR"));

            data.put("activePOs", poRepository.countByStatus(POStatus.OFFICIAL_PO));

            // 2. Active POs - ERROR FIX:
            // Yahan variable define karna zaroori tha taaki 'Cannot resolve' error na aaye
            List<POStatus> inactiveStatuses = java.util.Arrays.asList(
                    POStatus.COMPLETED,
                    POStatus.CANCELLED
            );

            // Sirf is line ko rakha hai jo sahi count (2) nikalegi
            long activeCount = poRepository.countByStatusNotIn(inactiveStatuses);
            data.put("activePOs", activeCount);


            // 3. Pending PRs (Aapka original code)
            data.put("pendingPRs", prRepository.countByStatus(POStatus.CREATED.name()));

            // 4. Reports (Aapka original code)
            data.put("spendingReport", getMonthlySpending());
            data.put("vendorReport", getVendorPerformance());
            data.put("fulfillmentRate", getFulfillmentRate());

        } catch (Exception e) {
            System.err.println("Dashboard Data Error: " + e.getMessage());
            e.printStackTrace();
        }

        return data;
    }

}