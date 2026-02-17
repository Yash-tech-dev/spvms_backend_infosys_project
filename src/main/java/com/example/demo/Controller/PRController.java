//package com.example.demo.Controller;
//
//import com.example.demo.Service.PRService;
//import com.example.demo.models.PurchaseRequest;
//import com.example.demo.models.PRItem; // Added this missing import
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//@RestController
//@CrossOrigin(origins = "http://localhost:3000") // Frontend connection allow karne ke liye
//@RequestMapping("/api/pr")
//
//public class PRController {
//
//    @Autowired
//    private PRService prService;
//
//    /**
//     * Method: createPR
//     * Working:
//     * 1. Receives the PR body and an optional quoteId from the URL.
//     * 2. Passes both to the service to ensure the database link is created immediately.
//     */
//    @PostMapping("/create")
//    public ResponseEntity<PurchaseRequest> createPR(
//            @RequestBody PurchaseRequest pr,
//            @RequestParam(required = false) Long quoteId) { // Add this parameter
//
//        // Now calling the service with BOTH arguments
//        return ResponseEntity.ok(prService.createPR(pr, quoteId));
//    }
//
//
//    // 2. Add Line Items (Requirement 1 & 5)
//    @PostMapping("/{prId}/items")
//    public ResponseEntity<PurchaseRequest> addItem(@PathVariable Long prId, @RequestBody PRItem item) {
//        // Calls the matching method in Service
//        return ResponseEntity.ok(prService.addItemToPR(prId, item));
//    }
//
//    // 3. Delete Item (Requirement 4)
//    @DeleteMapping("/{prId}/items/{itemId}")
//    public ResponseEntity<String> deleteItem(@PathVariable Long prId, @PathVariable Long itemId) {
//        // Calls the matching method in Service
//        prService.deleteItem(prId, itemId);
//        return ResponseEntity.ok("Item deleted and total cost recalculated.");
//    }
//}
//




///
// /////////////////////////////////////////////////////////



package com.example.demo.Controller;

import com.example.demo.Service.PRService;
import com.example.demo.models.PurchaseRequest;
import com.example.demo.models.PRItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List; // Zaroori hai list return karne ke liye

@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/api/pr")
public class PRController {

    @Autowired
    private PRService prService;

    // --- CRITICAL MISSING METHOD FOR FRONTEND ---
    /**
     * Method: getAllPRs
     * Working: Frontend (Dashboard) hits this method to show all the purchase orders in UI.
     */
    @GetMapping("/all")
    public ResponseEntity<List<PurchaseRequest>> getAllPRs() {
        return ResponseEntity.ok(prService.getAllPRs()); // Ensure your PRService has getAllPRs()
    }

    // 1. Create PR
    @PostMapping("/create")
    public ResponseEntity<PurchaseRequest> createPR(
            @RequestBody PurchaseRequest pr,
            @RequestParam(required = false) Long quoteId) {
        return ResponseEntity.ok(prService.createPR(pr, quoteId));
    }

    // 2. Add Line Items
    @PostMapping("/{prId}/items")
    public ResponseEntity<PurchaseRequest> addItem(@PathVariable Long prId, @RequestBody PRItem item) {
        return ResponseEntity.ok(prService.addItemToPR(prId, item));
    }

    // 3. Delete Item
    @DeleteMapping("/{prId}/items/{itemId}")
    public ResponseEntity<String> deleteItem(@PathVariable Long prId, @PathVariable Long itemId) {
        prService.deleteItem(prId, itemId);
        return ResponseEntity.ok("Item deleted and total cost recalculated.");
    }

// use to update the status of pr ( Pending or Approved )
    @PutMapping("/{id}/status")
    public ResponseEntity<PurchaseRequest> updateStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        return ResponseEntity.ok(prService.updatePRStatus(id, status));
    }

    /**
     * Method: updatePR
     * Working: ID path variable se leta hai aur Updated data RequestBody se.
     */
    @PutMapping("/update/{id}")
    public ResponseEntity<?> updatePR(@PathVariable Long id, @RequestBody PurchaseRequest prDetails) {
        try {
            // prService ka update logic call karein
            PurchaseRequest updatedPR = prService.updatePR(id, prDetails);
            return ResponseEntity.ok(updatedPR);
        } catch (Exception e) {
            // Agar error aaye toh 400 status bhejta hai
            return ResponseEntity.badRequest().body("Error updating PR: " + e.getMessage());
        }
    }

    @GetMapping("/my")
    public ResponseEntity<List<PurchaseRequest>> getMyPRs(Principal principal) {
        // principal.getName() hume wahi username dega jo login ke waqt use hua tha [cite: 2026-02-02]
        String loggedInUser = principal.getName();
        return ResponseEntity.ok(prService.getPRsByUsername(loggedInUser));
    }


}
