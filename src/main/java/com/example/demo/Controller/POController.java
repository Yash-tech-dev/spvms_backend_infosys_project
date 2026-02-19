package com.example.demo.Controller;

import com.example.demo.Service.POService;
import com.example.demo.models.DeliveryRecord;
import com.example.demo.models.POStatus;
import com.example.demo.models.PurchaseOrder;
import com.example.demo.Repository.PORepository;
import com.example.demo.models.PurchaseRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/po")
public class POController {

    @Autowired
    private POService poService;
    @Autowired
    private PORepository poRepository;

    /**
     * Requirement: Convert PR to PO.
     * Explanation: Takes an approved Purchase Request ID and generates a Purchase Order.
     * This populates the 'purchase_orders' table you saw in MySQL.
     */

    @GetMapping("/all")
    public ResponseEntity<List<PurchaseOrder>> getAllPurchaseOrders() {
        return ResponseEntity.ok(poRepository.findAll());
    }


    // Used to convert the PR into PO
    @PostMapping("/convert/{prId}")
    public ResponseEntity<?> convertPRtoPO(@PathVariable Long prId) {
        try {
            // Calling service method that contains your business logic
            PurchaseOrder createdPO = poService.createPOFromPR(prId);
            return ResponseEntity.ok(createdPO);
        } catch (Exception e) {
            // Log the error for debugging
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to convert PR to PO: " + e.getMessage());
        }
    }

    /**
     * Method: getPurchaseOrder
     * Explanation: Use this to check if the POStatus has changed to
     * PARTIAL_DELIVERY or COMPLETED after your POST request.
     */
    @GetMapping("/{id}")
    public ResponseEntity<PurchaseOrder> getPurchaseOrder(@PathVariable Long id) {
        return poRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

// used for download the particular PO with ID
    @GetMapping("/download/{id}")
    public ResponseEntity<byte[]> downloadIndividualPO(@PathVariable Long id) {
        byte[] pdfContent = poService.generateIndividualPOReport(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=PO_" + id + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfContent);
    }

    // used for download the all POs to present as WholePOReport
    @GetMapping("/download/all")
    public ResponseEntity<byte[]> downloadAllPOs(Principal principal) {
        byte[] excelContent = poService.generateWholePOReport(principal.getName());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=All_POs.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excelContent);
    }

    @GetMapping("/vendor/{vendorId}")
    public ResponseEntity<List<PurchaseOrder>> getOrdersByVendor(@PathVariable Long vendorId) {
        List<PurchaseOrder> orders = poRepository.findByVendorId(vendorId);
        if(orders.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(orders);
    }

    // Status update method ( to change the Accept/Deliver status)
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> statusMap) {
        return poRepository.findById(id).map(po -> {
            // METHOD EXPLANATION:
            // POStatus.valueOf() converts String into Enum  (e.g., "ACCEPTED" -> POStatus.ACCEPTED)
            String statusStr = statusMap.get("status").toUpperCase();
            po.setStatus(POStatus.valueOf(statusStr));

            poRepository.save(po);
            return ResponseEntity.ok("Status Updated Successfully");
        }).orElse(ResponseEntity.notFound().build());
    }


    @GetMapping("/my")
    public ResponseEntity<List<PurchaseOrder>> getMyPOs(Principal principal) {
        /** * Method Explanation:
         * 1. Principal: Security context se logged-in user ka username uthata hai. [cite: 2026-01-30]
         * 2. Security: No one can see others' POs by changing URL parameters. [cite: 2026-02-02]
         */
        String username = principal.getName();
        return ResponseEntity.ok(poService.getPOsByUsername(username));
    }

}