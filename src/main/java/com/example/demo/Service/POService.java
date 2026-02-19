package com.example.demo.Service;

import com.example.demo.Repository.DeliveryRepository;
import com.example.demo.Repository.POItemRepository;
import com.example.demo.Repository.PORepository;
import com.example.demo.Repository.PRRepository;
import com.example.demo.models.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static com.example.demo.models.POStatus.FULLY_DELIVERED;
import static com.example.demo.models.POStatus.PARTIALLY_DELIVERED;

/**
 * Service class managing the core Procurement lifecycle.
 * Handles the conversion of Purchase Requests (PR) to Official Purchase Orders (PO),
 * delivery confirmation, status transitions, and report generation.
 */
@Service
public class POService {

    @Autowired
    private PORepository poRepository;

    @Autowired
    private POItemRepository poItemRepository;

    @Autowired
    private DeliveryRepository deliveryRepository;

    @Autowired
    private PRRepository prRepository;

    /**
     * Method: createPOFromPR
     * Working:
     * Converts an approved Purchase Request into a formal Purchase Order.
     * 1. Validates the existence of the PR and assigned vendor.
     * 2. Maps PR header data to PO header data.
     * 3. Iterates through line items and persists them as POItems.
     * 4. Updates the source PR status to 'APPROVED'.
     * @param prId The ID of the Purchase Request to be converted.
     * @return The newly created and saved Purchase Order.
     */
    @Transactional
    public PurchaseOrder createPOFromPR(Long prId) {
        // 1. Fetch PR
        PurchaseRequest pr = prRepository.findById(prId)
                .orElseThrow(() -> new RuntimeException("PR not found with ID: " + prId));

        // 2. Create PO
        PurchaseOrder po = new PurchaseOrder();
        po.setStatus(POStatus.OFFICIAL_PO);
        po.setPurchaseRequest(pr);
        po.setTotalAmount(pr.getEstimatedTotalCost());
        po.setVendorQuoteRef("PR-REF-" + pr.getId());

        // --- CRITICAL FIX: Assign Vendor ---
        if (pr.getVendor() != null) {
            po.setVendor(pr.getVendor());
        } else {
            throw new RuntimeException("Error: PR #" + prId + " has no Vendor assigned. Cannot create PO.");
        }

        // Mapping items...
        if (pr.getItems() != null && !pr.getItems().isEmpty()) {
            PRItem firstItem = pr.getItems().get(0);
            po.setOrderedQuantity(firstItem.getQuantity());
            po.setUnitPrice(firstItem.getUnitPrice());
        }

        PurchaseOrder savedPO = poRepository.saveAndFlush(po);

        // Save items logic (same as before)...
        List<POItem> poItems = pr.getItems().stream().map(prItem -> {
            POItem poItem = new POItem();
            poItem.setItemName(prItem.getItemName());
            poItem.setOrderedQuantity(prItem.getQuantity());
            poItem.setUnitPrice(prItem.getUnitPrice());
            poItem.setPurchaseOrder(savedPO);
            return poItem;
        }).collect(Collectors.toList());

        poItemRepository.saveAll(poItems);
        savedPO.setItems(poItems);

        pr.setStatus("APPROVED");
        prRepository.save(pr);

        return poRepository.save(savedPO);
    }

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Method: confirmDelivery
     * Working:
     * Records a delivery event for a specific PO line item.
     * 1. Validates that the POItem exists.
     * 2. Calculates delivery value based on current unit price.
     * 3. Updates the item's aggregate delivered quantity (ignoring damaged goods).
     * 4. Recalculates the total 'Final Payable' amount for the entire Order.
     * 5. Triggers a status update for the parent PO.
     * @param itemId The ID of the POItem being received.
     * @param record The delivery data (quantity, condition, etc.).
     * @return The persisted delivery record.
     */
    @Transactional
    public DeliveryRecord confirmDelivery(Long itemId, DeliveryRecord record) {
        // 1. Fetch the PO Item using POITEM table ID
        POItem item = poItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item not found with ID: " + itemId +
                        ". Ensure you are sending POItem ID, not PurchaseOrder ID."));

        // 2. Set Basic Delivery Details
        record.setDeliveryDate(LocalDateTime.now());
        record.setPoItem(item);
        record.setPurchaseOrder(item.getPurchaseOrder());

        // 3. Lock the Price and Calculate row-level price
        if (item.getUnitPrice() != null) {
            record.setUnitPrice(item.getUnitPrice());
            record.setPriceAtDelivery(record.getQuantityReceived() * item.getUnitPrice());
        }

        // 4. Update Delivered Quantity only if NOT damaged
        if (record.getIsDamaged() != null && !record.getIsDamaged()) {
            int currentDelivered = (item.getDeliveredQuantity() == null) ? 0 : item.getDeliveredQuantity();
            int newTotal = currentDelivered + record.getQuantityReceived();

            // Validation: Cannot deliver more than what was ordered
            if (newTotal > item.getOrderedQuantity()) {
                throw new RuntimeException("Error: Quantity received (" + newTotal + ") exceeds ordered quantity (" + item.getOrderedQuantity() + ")!");
            }
            item.setDeliveredQuantity(newTotal);
        }

        // 5. CRITICAL: Save and Sync
        poItemRepository.save(item);
        DeliveryRecord savedRecord = deliveryRepository.saveAndFlush(record);

        // Force sync with DB
        entityManager.flush();

        // 6. Calculate Final Payable Amount for the entire PO
        PurchaseOrder po = item.getPurchaseOrder();
        entityManager.refresh(po);

        Double finalPayable = po.getItems().stream()
                .filter(pi -> pi.getDeliveryHistory() != null)
                .flatMap(pi -> pi.getDeliveryHistory().stream())
                .filter(d -> d.getIsDamaged() != null && !d.getIsDamaged())
                .mapToDouble(d -> {
                    Double up = (d.getUnitPrice() != null) ? d.getUnitPrice() : 0.0;
                    Integer qr = (d.getQuantityReceived() != null) ? d.getQuantityReceived() : 0;
                    return qr * up;
                })
                .sum();

        po.setFinalPayableAmount(finalPayable);
        poRepository.save(po);

        // 7. Auto-update PO Status
        updatePOStatus(po);

        return savedRecord;
    }

    /**
     * Method: updatePOStatus
     * Working:
     * Compares total ordered quantities against total received quantities
     * across all items to move the PO status between OFFICIAL, PARTIAL, and FULLY DELIVERED.
     * @param po The Purchase Order entity to be evaluated.
     */
    private void updatePOStatus(PurchaseOrder po) {
        // 1. Get total history from Deliveries table
        List<DeliveryRecord> history = deliveryRepository.findByPurchaseOrder(po);
        int totalReceived = history.stream().mapToInt(DeliveryRecord::getQuantityReceived).sum();

        // 2. Get original requirement from POItem table
        int totalOrdered = po.getItems().stream().mapToInt(POItem::getOrderedQuantity).sum();

        // 3. Automated Status Transition
        if (totalReceived >= totalOrdered) {
            po.setStatus(FULLY_DELIVERED);
        } else if (totalReceived > 0) {
            po.setStatus(PARTIALLY_DELIVERED);
        }
        poRepository.save(po);
    }

    /**
     * Generates a plain-text report for an individual Purchase Order.
     * @param id The ID of the Purchase Order.
     * @return Byte array representing the text report.
     */
    public byte[] generateIndividualPOReport(Long id) {
        PurchaseOrder po = poRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("PO Not Found"));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        StringBuilder sb = new StringBuilder();

        sb.append("PURCHASE ORDER REPORT\n");
        sb.append("------------------------------------------\n");
        sb.append("PO NUMBER         : ").append(po.getPoNumber()).append("\n");
        sb.append("SOURCE PR         : ").append(po.getSourcePr()).append("\n");
        sb.append("VENDOR            : ").append(po.getVendor() != null ? po.getVendor().getCompanyName() : "N/A").append("\n");
        sb.append("VENDOR QUOTE REF  : ").append(po.getQuoteRef()).append("\n");
        sb.append("TOTAL AMOUNT      : ₹").append(po.getTotalAmount()).append("\n");
        sb.append("STATUS            : ").append(po.getStatus()).append("\n");
        sb.append("------------------------------------------\n");

        out.writeBytes(sb.toString().getBytes());
        return out.toByteArray();
    }

    /**
     * Generates a CSV-format report of all Purchase Orders in the system.
     * @param username The identifier of the requesting user.
     * @return Byte array representing the CSV data.
     */
    public byte[] generateWholePOReport(String username) {
        List<PurchaseOrder> allOrders = poRepository.findAll();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        StringBuilder csvContent = new StringBuilder();

        // Header row
        csvContent.append("PO Number,Source PR,Vendor,Quote Ref,Total Amount,Status\n");

        // Data rows
        for (PurchaseOrder po : allOrders) {
            csvContent.append(po.getPoNumber()).append(",")
                    .append(po.getSourcePr()).append(",")
                    .append(po.getVendor() != null ? po.getVendor().getCompanyName() : "N/A").append(",")
                    .append(po.getQuoteRef()).append(",")
                    .append(po.getTotalAmount()).append(",")
                    .append(po.getStatus()).append("\n");
        }

        out.writeBytes(csvContent.toString().getBytes());
        return out.toByteArray();
    }

    public List<PurchaseOrder> getPOsByUsername(String username) {
        return poRepository.findByCreatedByUsername(username);
    }
}