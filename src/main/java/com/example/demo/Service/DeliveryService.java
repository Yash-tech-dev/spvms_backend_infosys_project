package com.example.demo.Service;

import com.example.demo.Repository.DeliveryRepository;
import com.example.demo.Repository.POItemRepository;
import com.example.demo.Repository.PORepository;
import com.example.demo.models.DeliveryRecord;
import com.example.demo.models.POItem;
import com.example.demo.models.PurchaseOrder;
import com.example.demo.models.POStatus; // Import the Enum
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List; // Required for List
import java.util.stream.Collectors; // Required for Stream logic

import static com.example.demo.models.POStatus.FULLY_DELIVERED;
import static com.example.demo.models.POStatus.PARTIALLY_DELIVERED;
@Service
public class DeliveryService {

    @Autowired
    private DeliveryRepository deliveryRepository;

    @Autowired
    private PORepository poRepository;

    @Autowired
    private POItemRepository poItemRepository;

    /**
     * Method: recordDelivery
     * Working:
     * 1. Fetches the PO and the relevant Item.
     * 2. Links the record to the Item and PO.
     * 3. Calls calculateDeliveryValue() to set unitPrice and priceAtDelivery.
     * 4. Updates cumulative delivered quantity on the POItem.
     * 5. Triggers the updatePOStatus logic to refresh the order status.
     */
//    @Transactional
//    public DeliveryRecord recordDelivery(Long poId, DeliveryRecord record) {
//        PurchaseOrder po = poRepository.findById(poId)
//                .orElseThrow(() -> new RuntimeException("Purchase Order not found"));
//
//        // Match the first item (Logic can be updated to match specific IDs)
//        POItem item = po.getItems().stream()
//                .findFirst()
//                .orElseThrow(() -> new RuntimeException("No items found in PO"));
//
//        // 1. Link references
//        record.setPurchaseOrder(po);
//        record.setPoItem(item);
//
//        // 2. Calculate values (Crucial for Postman output)
//        // This sets BOTH this.unitPrice and this.priceAtDelivery in the Entity
//        record.calculateDeliveryValue();
//
//        // 3. Update the item's delivered quantity
//        int currentDelivered = (item.getDeliveredQuantity() != null) ? item.getDeliveredQuantity() : 0;
//        item.setDeliveredQuantity(currentDelivered + record.getQuantityReceived());
//
//        // 4. Save the updated Item (Inventory update)
//        poItemRepository.save(item);
//
//        // 5. Save the Delivery Record
//        DeliveryRecord savedRecord = deliveryRepository.save(record);
//
//        // 6. Update the overall PO Status (e.g., CREATED -> PARTIALLY_DELIVERED)
//        updatePOStatus(po);
//
//        return savedRecord;
//    }

    @Transactional
    public DeliveryRecord recordDelivery(Long itemId, DeliveryRecord record) {
        System.out.println("Attempting delivery for Item ID: " + itemId);
        // 1. Fetch the Item: URL ki ID (itemId) se database se specific item nikaalte hain.
        POItem item = poItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("PO Item not found with ID: " + itemId));

        PurchaseOrder po = item.getPurchaseOrder();

        // 2. Validation Check (CRUCIAL): Quantity save karne se PEHLE check karna zaroori hai.
        int alreadyDelivered = (item.getDeliveredQuantity() != null) ? item.getDeliveredQuantity() : 0;
        int newPotentialTotal = alreadyDelivered + record.getQuantityReceived();

        if (newPotentialTotal > item.getOrderedQuantity()) {
            throw new RuntimeException("Error: Total delivery (" + newPotentialTotal +
                    ") exceeds Ordered quantity (" + item.getOrderedQuantity() + ")");
        }

        // 3. Link References: Record ko Item aur PO ke saath map karte hain.
        record.setPurchaseOrder(po);
        record.setPoItem(item);

        // 4. Price Locking: Sprint 4 ka unitPrice yahan copy karte hain taaki delivery value correct ho.
        record.setUnitPrice(item.getUnitPrice());
        record.calculateDeliveryValue(); // (Qty Received * Unit Price) compute karta hai.

        // 5. Update Item Quantity: Validation pass hone ke baad item ki quantity update karte hain.
        item.setDeliveredQuantity(newPotentialTotal);
        poItemRepository.save(item);

        // 6. Save Delivery Record: Database mein shipment ka record save karte hain.
        DeliveryRecord savedRecord = deliveryRepository.save(record);

        // 7. Trigger Status Update: Pure PO ka status (Partial/Full) refresh karte hain.
        updatePOStatus(po);

        return savedRecord;
    }
//    private void updatePOStatus(PurchaseOrder po) {
//        List<DeliveryRecord> history = deliveryRepository.findByPurchaseOrder(po);
//
//        int totalReceived = history.stream()
//                .mapToInt(DeliveryRecord::getQuantityReceived)
//                .sum();
//
//        // Calculate total ordered quantity across all items
//        int totalOrdered = po.getItems().stream()
//                .mapToInt(POItem::getOrderedQuantity)
//                .sum();
//
//        if (totalReceived >= totalOrdered) {
//            po.setStatus(FULLY_DELIVERED);
//        } else if (totalReceived > 0) {
//            po.setStatus(PARTIALLY_DELIVERED);
//        }
//
//        poRepository.save(po);

    private void updatePOStatus(PurchaseOrder po) {
        // CHANGE 4: Status check based on all items in the PO
        int totalOrdered = po.getItems().stream()
                .mapToInt(POItem::getOrderedQuantity)
                .sum();

        int totalDelivered = po.getItems().stream()
                .mapToInt(i -> i.getDeliveredQuantity() != null ? i.getDeliveredQuantity() : 0)
                .sum();

        if (totalDelivered >= totalOrdered) {
            po.setStatus(POStatus.FULLY_DELIVERED);
        } else if (totalDelivered > 0) {
            po.setStatus(POStatus.PARTIALLY_DELIVERED);
        }

        poRepository.save(po);
    }


    /**
     * Method: getHistoryByPoId
     * Working:
     * 1. Finds the Purchase Order by its ID to ensure it exists.
     * 2. Queries the DeliveryRepository using the findByPurchaseOrder method we created earlier.
     * 3. Returns a list of all shipments (timelines) recorded for this specific PO.
     */
    public List<DeliveryRecord> getHistoryByPoId(Long poId) {
        // Check if the PO exists first
        PurchaseOrder po = poRepository.findById(poId)
                .orElseThrow(() -> new RuntimeException("Purchase Order #" + poId + " not found."));

        // Return all records associated with this PO
        return deliveryRepository.findByPurchaseOrder(po);
    }




}