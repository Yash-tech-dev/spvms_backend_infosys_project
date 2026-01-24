//package com.example.demo.models;
//
//import com.fasterxml.jackson.annotation.JsonBackReference;
//import com.fasterxml.jackson.annotation.JsonProperty;
//import jakarta.persistence.*;
//import java.time.LocalDateTime;
//
//@Entity
//@Table(name = "deliveries")
//public class DeliveryRecord {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @Column(nullable = false)
//    private LocalDateTime deliveryDate; // Sprint 5: Tracks the timeline
//
//    @Column(nullable = false)
//    private Integer quantityReceived; // Sprint 5: Supports partial delivery logic
//
//    private String remarks; // Sprint 5: For damaged/returned item notes
//
//    private String deliveryStatus; // e.g., RECEIVED, PARTIAL, RETURNED
//
//    private Boolean isDamaged; // Sprint 5: Flag for quality control
//
//
//    @JsonProperty("price_at_delivery")
//    private Double priceAtDelivery;
//    // Relationship to the specific Item
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "po_item_id")
//    @JsonBackReference(value = "item-deliveries") // The "Back" reference - stops the loop
//    private POItem poItem;
//
//    // FIX: Added missing JPA annotations
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "po_id")
//    @JsonBackReference(value = "po-deliveries") // FIX: Added this to break the loop to PurchaseOrder
//    private PurchaseOrder purchaseOrder;
//    private Double unitPrice;
//
//    // Method to calculate the value dynamically
//    public void calculateDeliveryValue() {
//        if (this.poItem != null && this.quantityReceived != null) {
//            // Safe check: if unitPrice is null, default to 0.0 to avoid NPE
//            Double price = (this.poItem.getUnitPrice() != null) ? this.poItem.getUnitPrice() : 0.0;
//            this.priceAtDelivery = this.quantityReceived * price;
//        }
//    }
//
//    /**
//     * Method: onCreate
//     * Working: This is a JPA Lifecycle callback.
//     * It automatically triggers right before the object is persisted
//     * to the database, ensuring deliveryDate is never null.
//     */
//
//    @PrePersist
//    protected void onCreate() {
//        if (this.deliveryDate == null) {
//            this.deliveryDate = LocalDateTime.now();
//        }}
//    public DeliveryRecord() {}
//
//
//
//
//    // --- WORKING METHODS ---
//
//    /** * Method: setPurchaseOrder
//     * Working: Explicitly links this delivery record to a parent Purchase Order.
//     * This is used by the Service layer during the "Confirm Delivery" process.
//     */
//    public void setPurchaseOrder(PurchaseOrder purchaseOrder) {
//        this.purchaseOrder = purchaseOrder;
//    }
//
//    /** * Method: setPoItem
//     * Working: Links the delivery to a specific line item. Essential for
//     * partial delivery support where items are shipped separately.
//     */
//    public void setPoItem(POItem poItem) {
//        this.poItem = poItem;
//    }
//
//    // --- Getters and Setters ---
//    public Long getId() { return id; }
//    public void setId(Long id) { this.id = id; }
//
//    public LocalDateTime getDeliveryDate() { return deliveryDate; }
//    public void setDeliveryDate(LocalDateTime deliveryDate) { this.deliveryDate = deliveryDate; }
//
//    public Integer getQuantityReceived() { return quantityReceived; }
//    public void setQuantityReceived(Integer quantityReceived) { this.quantityReceived = quantityReceived; }
//
//    public String getRemarks() { return remarks; }
//    public void setRemarks(String remarks) { this.remarks = remarks; }
//
//    public String getDeliveryStatus() { return deliveryStatus; }
//    public void setDeliveryStatus(String deliveryStatus) { this.deliveryStatus = deliveryStatus; }
//
//    public Boolean getIsDamaged() { return isDamaged; }
//    public void setIsDamaged(Boolean isDamaged) { this.isDamaged = isDamaged; }
//
//    public POItem getPoItem() { return poItem; }
//
//    public PurchaseOrder getPurchaseOrder() { return purchaseOrder; }
//    public Double getUnitPrice() {
//        return unitPrice;
//    }
//
//}

package com.example.demo.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity representing a record of goods received against a Purchase Order.
 * It tracks delivery dates, quantities, quality (damage), and financial values.
 */
@Entity
@Table(name = "deliveries")
public class DeliveryRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime deliveryDate;

    @Column(nullable = false)
    private Integer quantityReceived;

    private String remarks;
    private String deliveryStatus;
    private Boolean isDamaged;

    /**
     * Stores the total monetary value of the delivery (Quantity Received * Unit Price).
     * Mapped to 'price_at_delivery' in JSON.
     */
    @JsonProperty("price_at_delivery")
    private Double priceAtDelivery;

    /**
     * Captures the price per unit at the specific moment of delivery.
     */
    private Double unitPrice;

    /**
     * Link to the specific line item in the Purchase Order.
     * Uses LAZY fetching for performance and JsonBackReference to prevent infinite JSON loops.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "po_item_id")
    @JsonBackReference(value = "item-deliveries")
    private POItem poItem;

    /**
     * Link to the overall Purchase Order this delivery belongs to.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "po_id")
    @JsonBackReference(value = "po-deliveries")
    private PurchaseOrder purchaseOrder;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    /**
     * Default constructor for JPA.
     */
    public DeliveryRecord() {}

    /**
     * Method: calculateDeliveryValue
     * Working: Pulls the unit price from the linked POItem and calculates
     * the total delivery value. This prevents the 0.0 value in your output.
     * It ensures data consistency between the Item price and the Delivery record.
     */
    public void calculateDeliveryValue() {
        if (this.poItem != null && this.poItem.getUnitPrice() != null) {
            this.unitPrice = this.poItem.getUnitPrice();
            if (this.quantityReceived != null) {
                this.priceAtDelivery = this.quantityReceived * this.unitPrice;
            }
        }
    }

    /**
     * Lifecycle callback that runs before saving a new record.
     * Ensures the deliveryDate is set to current time if not provided.
     */
    @PrePersist
    protected void onCreate() {
        if (this.deliveryDate == null) {
            this.deliveryDate = LocalDateTime.now();
        }
    }

    // --- GETTERS AND SETTERS ---

    /** @return the unique delivery ID */
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    /** @return the date and time when delivery occurred */
    public LocalDateTime getDeliveryDate() { return deliveryDate; }
    public void setDeliveryDate(LocalDateTime deliveryDate) { this.deliveryDate = deliveryDate; }

    /** @return total items received in this batch */
    public Integer getQuantityReceived() { return quantityReceived; }
    public void setQuantityReceived(Integer quantityReceived) { this.quantityReceived = quantityReceived; }

    /** @return additional notes regarding the delivery */
    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    /** @return the status string (e.g., Pending, Completed) */
    public String getDeliveryStatus() { return deliveryStatus; }
    public void setDeliveryStatus(String deliveryStatus) { this.deliveryStatus = deliveryStatus; }

    /** @return true if items were reported damaged */
    public Boolean getIsDamaged() { return isDamaged; }
    public void setIsDamaged(Boolean isDamaged) { this.isDamaged = isDamaged; }

    /** @return the total calculated value of this specific delivery */
    public Double getPriceAtDelivery() { return priceAtDelivery; }
    public void setPriceAtDelivery(Double priceAtDelivery) { this.priceAtDelivery = priceAtDelivery; }

    /** @return the unit price used for this delivery calculation */
    public Double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(Double unitPrice) { this.unitPrice = unitPrice; }

    /** @return the associated PO item */
    public POItem getPoItem() { return poItem; }
    public void setPoItem(POItem poItem) { this.poItem = poItem; }

    /** @return the parent Purchase Order */
    public PurchaseOrder getPurchaseOrder() { return purchaseOrder; }
    public void setPurchaseOrder(PurchaseOrder purchaseOrder) { this.purchaseOrder = purchaseOrder; }
}