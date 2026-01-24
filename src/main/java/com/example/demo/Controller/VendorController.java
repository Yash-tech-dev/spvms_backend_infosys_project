package com.example.demo.Controller;

import com.example.demo.Repository.UserRepository;
import com.example.demo.Repository.VendorRepository;
import com.example.demo.Service.DocumentService;
import com.example.demo.Service.POService;
import com.example.demo.models.DeliveryRecord;
import com.example.demo.models.User;
import com.example.demo.models.Vendor;
import com.example.demo.models.VendorDocument;
import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.List;

/**
 * REST Controller for Vendor-related operations.
 * Handles vendor management, document uploads, and delivery confirmations.
 */
@RestController
@RequestMapping("/api/vendor")
public class VendorController {

    @Autowired
    private VendorRepository vendorRepository;

    @Autowired
    private DocumentService documentService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private POService poService;

    /**
     * Retrieves all users who have the 'ROLE_VENDOR' role.
     * It filters the total user list to return only those registered as vendors.
     * @return A list of User objects with the vendor role.
     */
    @GetMapping("/all")
    public ResponseEntity<List<User>> getAllVendors() { // Change Vendor to User here
        try {
            // We use UserRepository because that is where the vendor data resides
            List<User> vendors = userRepository.findAll()
                    .stream()
                    .filter(user -> "ROLE_VENDOR".equalsIgnoreCase(user.getRole()))
                    .toList();

            return ResponseEntity.ok(vendors); // Return types now match
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Handles the uploading of documents for the currently authenticated vendor.
     * @param file The multipart file to be uploaded.
     * @param principal The security principal used to extract the current username.
     * @return A success message or an internal server error status.
     */
    @PostMapping("/upload")
    public ResponseEntity<String> uploadDocument(@RequestParam("file") MultipartFile file, Principal principal) {
        try {
            // principal.getName() gets the username from the JWT token automatically!
            User user = userRepository.findByUsername(principal.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            documentService.storeDocument(file, user);
            return ResponseEntity.ok("File uploaded successfully: " + file.getOriginalFilename());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Could not upload file");
        }
    }

    /**
     * Simple endpoint to verify access to the Vendor Dashboard.
     * @return A greeting string for the vendor portal.
     */
    @GetMapping("/dashboard")
    public String getVendorDashboard() {
        return "Vendor Portal: Access Granted. Welcome to your vendor dashboard.";
    }

    /**
     * Toggles the 'active' status of a user (vendor) between true and false.
     * @param id The unique ID of the user.
     * @return The updated User object or a 404 Not Found response.
     */
    @CrossOrigin(origins = "http://localhost:3000", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PATCH, RequestMethod.PUT, RequestMethod.OPTIONS})
    @PatchMapping("/{id}/toggle-status")
    public ResponseEntity<User> toggleStatus(@PathVariable Long id) {
        return userRepository.findById(id).map(user -> {
            // Method Working: Inverts the current boolean value
            user.setActive(!user.isActive());
            userRepository.save(user);
            return ResponseEntity.ok(user);
        }).orElse(ResponseEntity.notFound().build());
    }

    /**
     * Registers a new vendor in the system.
     * Forces the role to 'ROLE_VENDOR' and sets the account to active by default.
     * @param newUser The user details provided in the request body.
     * @return The saved User object or a 400 Bad Request status.
     */
    @PostMapping("/add")
    public ResponseEntity<User> addVendor(@RequestBody User newUser) {
        try {
            newUser.setRole("ROLE_VENDOR"); // Forcefully set the vendor role
            newUser.setActive(true);
            // Note: In a production app, password encryption is required here
            User savedUser = userRepository.save(newUser);
            return ResponseEntity.ok(savedUser);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * Requirement: Add delivery confirmation API.
     * Explanation: This is the endpoint called to record a delivery for a specific PO item.
     * Path: POST http://localhost:8081/api/po/items/{itemId}/deliver
     * @param itemId The ID of the Purchase Order item being delivered.
     * @param record The delivery details provided in the request body.
     * @return The created DeliveryRecord object.
     */
    @PostMapping("/items/{itemId}/deliver")
    public ResponseEntity<DeliveryRecord> confirmDelivery(
            @PathVariable Long itemId,
            @RequestBody DeliveryRecord record) {
        return ResponseEntity.ok(poService.confirmDelivery(itemId, record));
    }
}