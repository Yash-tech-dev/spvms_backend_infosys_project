//package com.example.demo.Service;
//
//import com.example.demo.Repository.PRRepository;
//import com.example.demo.Repository.PRItemRepository;
//import com.example.demo.models.PRItem;
//import com.example.demo.models.PurchaseRequest;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//@Service
//public class PRService {
//
//    @Autowired
//    private PRRepository prRepository;
//
//    // Requirement: Create a blank PR
//    public PurchaseRequest createPR(PurchaseRequest pr) {
//        return prRepository.save(pr);
//    }
//
//    // Requirement: Add & Update PR line items + Validate item price and quantity
//    @Transactional
//    public PurchaseRequest addItemToPR(Long prId, PRItem newItem) {
//        // Validation (Requirement 2)
//        if (newItem.getQuantity() == null || newItem.getQuantity() <= 0 ||
//                newItem.getUnitPrice() == null || newItem.getUnitPrice() <= 0) {
//            throw new RuntimeException("Quantity and Price must be greater than zero!");
//        }
//
//        PurchaseRequest pr = prRepository.findById(prId)
//                .orElseThrow(() -> new RuntimeException("PR not found"));
//
//        // Calculation (Requirement 5)
//        newItem.setTotalPrice(newItem.getQuantity() * newItem.getUnitPrice());
//        newItem.setPurchaseRequest(pr);
//
//        pr.getItems().add(newItem);
//        pr.calculateTotal(); // Updates estimatedTotalCost
//
//        return prRepository.save(pr);
//    }
//
//    // Requirement: Implement delete item logic
//    @Transactional
//    public void deleteItem(Long prId, Long itemId) {
//        PurchaseRequest pr = prRepository.findById(prId)
//                .orElseThrow(() -> new RuntimeException("PR not found"));
//
//        // Remove the item from the list (orphanRemoval in Entity will delete it from DB)
//        pr.getItems().removeIf(item -> item.getId().equals(itemId));
//
//        // Recalculate cost after deletion
//        pr.calculateTotal();
//
//        prRepository.save(pr);
//    }
//}
package com.example.demo.Service;

import com.example.demo.Repository.DocumentRepository;
import com.example.demo.Repository.PRRepository;
import com.example.demo.Repository.PRItemRepository;
import com.example.demo.models.PRItem;
import com.example.demo.models.PurchaseRequest;
import com.example.demo.models.VendorDocument;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PRService {

    @Autowired
    private PRRepository prRepository;

    @Autowired
    private PRItemRepository prItemRepository;

    @Autowired
    private DocumentRepository quoteRepository; // Sprint 4 Requirement: Connection to Sprint 3 Docs

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Requirement: Create PR and link Quote.
     * Fix: Added @Transactional and fixed the return logic.
     */
    @Transactional // CRITICAL: Refresh only works within a transaction
    public PurchaseRequest createPR(PurchaseRequest pr, Long quoteId) {
        // 1. Link the Quote from Sprint 3
        if (quoteId != null) {
            VendorDocument quote = quoteRepository.findById(quoteId)
                    .orElseThrow(() -> new RuntimeException("Quote not found with ID: " + quoteId));
            pr.setQuote(quote);
        }

        // 2. Sprint 4: Calculate Line Items
        if (pr.getItems() != null && !pr.getItems().isEmpty()) {
            for (PRItem item : pr.getItems()) {
                item.setTotalPrice(item.getQuantity() * item.getUnitPrice());
                item.setPurchaseRequest(pr);
            }
            pr.calculateTotal(); // Requirement 5: Grand Total
        }

        // 3. Save to Database
        PurchaseRequest savedPr = prRepository.saveAndFlush(pr);

        // 4. FIX FOR NULL VALUES: Reloading full document metadata (fileName, fileType)
        // This ensures Postman shows real data instead of null
        entityManager.refresh(savedPr);

        return savedPr;
    }

    /**
     * Requirement: Add item to existing PR.
     */
    @Transactional
    public PurchaseRequest addItemToPR(Long prId, PRItem newItem) {
        if (newItem.getQuantity() == null || newItem.getQuantity() <= 0 ||
                newItem.getUnitPrice() == null || newItem.getUnitPrice() <= 0) {
            throw new RuntimeException("Quantity and Price must be greater than zero!");
        }

        PurchaseRequest pr = prRepository.findById(prId)
                .orElseThrow(() -> new RuntimeException("PR not found"));

        newItem.setTotalPrice(newItem.getQuantity() * newItem.getUnitPrice());
        newItem.setPurchaseRequest(pr);

        pr.getItems().add(newItem);
        pr.calculateTotal();

        return prRepository.save(pr);
    }

    /**
     * Requirement: Delete item and recalculate.
     */
    @Transactional
    public void deleteItem(Long prId, Long itemId) {
        PurchaseRequest pr = prRepository.findById(prId)
                .orElseThrow(() -> new RuntimeException("PR not found"));

        pr.getItems().removeIf(item -> item.getId().equals(itemId));
        pr.calculateTotal();

        prRepository.save(pr);
    }

    // to find all the prs

    public List<PurchaseRequest> getAllPRs() {
        return prRepository.findAll(); // standard JPA method
    }


// To used to update the PR status (Approved or Rejected)
    @Transactional
    public PurchaseRequest updatePRStatus(Long id, String status) {
        PurchaseRequest pr = prRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Purchase Request not found with ID: " + id));

        pr.setStatus(status); // Updates status to APPROVED or REJECTED
        return prRepository.save(pr);
    }


    /**
     * Method: updatePR
     * Working:
     * 1. Database se purana PR dhundta hai.
     * 2. Agar mil jata hai, toh uski description aur items update karta hai.
     * 3. Frontend se aaye Total Cost ko database mein save karta hai.
     */
    public PurchaseRequest updatePR(Long id, PurchaseRequest updatedData) {
        // 1. Pehle check karo ki PR exist karta hai ya nahi
        return prRepository.findById(id).map(existingPR -> {

            // 2. Basic fields update karo
            existingPR.setDescription(updatedData.getDescription());
            existingPR.setEstimatedTotalCost(updatedData.getEstimatedTotalCost());

            // 3. Items update karo (PR aur PRItem ka relationship handle karna)
            if (updatedData.getItems() != null && !updatedData.getItems().isEmpty()) {
                // Purane items ko clear karke naye add karna sabse safe approach hai simple apps ke liye
                existingPR.getItems().clear();

                updatedData.getItems().forEach(item -> {
                    item.setPurchaseRequest(existingPR); // Link back to PR
                    existingPR.getItems().add(item);
                });
            }

            // 4. Save and Return
            return prRepository.save(existingPR);
        }).orElseThrow(() -> new RuntimeException("PR not found with id " + id));
    }

    public List<PurchaseRequest> getPRsByUsername(String username) {
        return prRepository.findByCreatedByUsername(username);
    }

}