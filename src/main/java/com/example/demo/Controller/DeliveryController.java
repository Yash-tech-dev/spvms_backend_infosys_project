package com.example.demo.Controller;

import com.example.demo.models.DeliveryRecord;
import com.example.demo.Service.DeliveryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/deliveries")
public class DeliveryController {

    @Autowired
    private DeliveryService deliveryService;

    /**
     * Method: confirmDelivery
     * Endpoint: POST /api/deliveries/confirm/{poId}
     * Working:
     * 1. Receives the Purchase Order ID in the URL.
     * 2. Receives a JSON body containing delivery date, quantity, and remarks.
     * 3. Calls the service to save the record and automatically update the PO status
     * (to PARTIALLY_DELIVERED or FULLY_DELIVERED).
     */
    @PostMapping("/confirm/{poId}")
    public ResponseEntity<DeliveryRecord> confirmDelivery(
            @PathVariable Long poId,
            @RequestBody DeliveryRecord deliveryRecord) {

        DeliveryRecord savedRecord = deliveryService.recordDelivery(poId, deliveryRecord);
        return ResponseEntity.ok(savedRecord);
    }

    /**
     * Method: getDeliveryHistory
     * Endpoint: GET /api/deliveries/history/{poId}
     * Working:
     * Returns all delivery records associated with a specific Purchase Order.
     * This is useful for verifying the timeline and checking remarks for damaged items.
     */
    @GetMapping("/history/{poId}")
    public ResponseEntity<List<DeliveryRecord>> getDeliveryHistory(@PathVariable Long poId) {
        // This assumes you add a 'getHistoryByPoId' method to your service
        return ResponseEntity.ok(deliveryService.getHistoryByPoId(poId));
    }
}