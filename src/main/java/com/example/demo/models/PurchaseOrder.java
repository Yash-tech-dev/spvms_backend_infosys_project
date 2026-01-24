//package com.example.demo.models;
//
//import com.fasterxml.jackson.annotation.JsonManagedReference;
//import jakarta.persistence.*;
//import java.util.List;
//import java.util.ArrayList;
//
//@Entity
//@Table(name = "purchase_orders")
//public class PurchaseOrder {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @Enumerated(EnumType.STRING)
//    private POStatus status = POStatus.CREATED;
//
//    @OneToOne
//    @JoinColumn(name = "pr_id", nullable = false)
//    private PurchaseRequest purchaseRequest;
//
//    /**
//     * Method: getItems
//     * Working: Added @JsonManagedReference. This tells Jackson that PO is the owner
//     * of the item list and should serialize the items.
//     */
//    @OneToMany(mappedBy = "purchaseOrder", cascade = CascadeType.ALL)
//    @JsonManagedReference(value = "po-items")
//    private List<POItem> items = new ArrayList<>();
//
//    /**
//     * Working: You likely have a relationship to DeliveryRecord here too.
//     * Use @JsonManagedReference to allow seeing deliveries when viewing a PO.
//     */
//    @OneToMany(mappedBy = "purchaseOrder", cascade = CascadeType.ALL)
//    @JsonManagedReference(value = "po-deliveries")
//    private List<DeliveryRecord> deliveries = new ArrayList<>();
//
//    @ManyToOne
//    @JoinColumn(name = "vendor_id", nullable = false, referencedColumnName = "id")
//    private User vendor;
//
//    private Double totalAmount;
//    private Integer orderedQuantity;
//    private Double unitPrice;
//
//    public PurchaseOrder() {}
//
//    // --- WORKING METHODS ---
//
//    /**
//     * Method: getTotalItemPrice
//     * Working: Calculates line item total. Used for internal logic and
//     * appearing in the JSON response.
//     */
//
//
//    /**
//     * Method: getTotalPrice
//     * Explanation: This calculates the total value of this specific item line
//     * (e.g., 5 Laptops * 1000 = 5000).
//     */
//    public Double getTotalItemPrice() {
//        return (this.unitPrice != null && this.orderedQuantity != null) ? this.unitPrice * this.orderedQuantity : 0.0;
//    }
//
//    // --- Getters and Setters ---
//    public Long getId() { return id; }
//    public void setId(Long id) { this.id = id; }
//
//    public POStatus getStatus() { return status; }
//    public void setStatus(POStatus status) { this.status = status; }
//
//    public List<POItem> getItems() { return items; }
//    public void setItems(List<POItem> items) { this.items = items; }
//
//    public void setTotalAmount(Double totalAmount) { this.totalAmount = totalAmount; }
//    public Double getTotalAmount() { return totalAmount; }
//
//    public void setVendor(User vendor) { this.vendor = vendor; }
//    public User getVendor() { return vendor; }
//
//    public void setPurchaseRequest(PurchaseRequest purchaseRequest) { this.purchaseRequest = purchaseRequest; }
//    public PurchaseRequest getPurchaseRequest() { return purchaseRequest; }
//
//    public List<DeliveryRecord> getDeliveries() { return deliveries; }
//    public void setDeliveries(List<DeliveryRecord> deliveries) { this.deliveries = deliveries; }
//}
//
///**
// * Method: getTotalPrice
// * Explanation: This calculates the total value of this specific item line
// * (e.g., 5 Laptops * 1000 = 5000).
// */

package com.example.demo.models;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

/**
 * Entity representing a Purchase Order (PO).
 * This class serves as the central hub for procurement, linking Purchase Requests,
 * line items, vendors, and delivery tracking.
 */
@Entity
@Table(name = "purchase_orders")
public class PurchaseOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Current state of the PO (e.g., CREATED, APPROVED, COMPLETED). */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 500)
    private POStatus status = POStatus.CREATED;

    /** The original Purchase Request that triggered this order. */
    @OneToOne
    @JoinColumn(name = "pr_id", nullable = false)
    private PurchaseRequest purchaseRequest;

    /** * List of specific items included in this order.
     * @JsonManagedReference indicates this is the parent side of the POItem relationship.
     */
    @OneToMany(mappedBy = "purchaseOrder", cascade = CascadeType.ALL)
    @JsonManagedReference(value = "po-items")
    private List<POItem> items = new ArrayList<>();

    /** * History of deliveries received for this specific order.
     * @JsonManagedReference prevents infinite recursion while allowing delivery visibility.
     */
    @OneToMany(mappedBy = "purchaseOrder", cascade = CascadeType.ALL)
    @JsonManagedReference(value = "po-deliveries")
    private List<DeliveryRecord> deliveries = new ArrayList<>();

    /** The vendor assigned to fulfill this order. Allows null for draft stages. */
    @ManyToOne(optional = true) // Isse null value allow ho jayegi
    @JoinColumn(name = "vendor_id", nullable = true)
    private User vendor;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Column(name = "ordered_quantity")
    private Integer orderedQuantity;

    @Column(name = "unit_price")
    private Double unitPrice;

    /** Reference to the vendor's quotation document. */
    @Column(name = "vendor_quote_ref")
    private String vendorQuoteRef;

    /** The total calculated amount of the order based on all line items. */
    private Double totalAmount;

    public PurchaseOrder() {}

    /** The actual amount to be paid based on successfully received (non-damaged) goods. */
    @Column(name = "final_payable_amount")
    private Double finalPayableAmount = 0.0;

    @Column(name = "po_number")
    private String poNumber;

    @Column(name = "source_pr")
    private String sourcePr;

    @Column(name = "quote_ref")
    private String quoteRef; // Isme 'jsRoadmap1.pdf' store hoga

    // --- WORKING METHODS ---

    /**
     * Method: calculateGrandTotal
     * Working: Iterates through all linked POItems, multiplies their unit price
     * by ordered quantity, and sums them up to determine the total order value.
     */
    public void calculateGrandTotal() {
        if (this.items != null) {
            this.totalAmount = this.items.stream()
                    .mapToDouble(item -> (item.getUnitPrice() != null && item.getOrderedQuantity() != null)
                            ? item.getUnitPrice() * item.getOrderedQuantity() : 0.0)
                    .sum();
        }
    }

    /**
     * Method: calculateFinalPayable
     * Working: Calculates the total cost of goods actually received in good condition.
     * It filters out any damaged items and only sums items marked as not damaged.
     */
    public void calculateFinalPayable() {
        if (this.items != null) {
            this.finalPayableAmount = this.items.stream()
                    .flatMap(item -> item.getDeliveryHistory().stream())
                    .filter(delivery -> delivery.getIsDamaged() != null && !delivery.getIsDamaged())
                    .mapToDouble(delivery -> delivery.getQuantityReceived() * delivery.getUnitPrice())
                    .sum();
        }}

    // --- Getters and Setters ---

    /** @return the unique ID of the Purchase Order */
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    /** @return the current lifecycle status of the PO */
    public POStatus getStatus() { return status; }
    public void setStatus(POStatus status) { this.status = status; }

    /** @return the list of items associated with this PO */
    public List<POItem> getItems() { return items; }
    public void setItems(List<POItem> items) { this.items = items; }

    public void setTotalAmount(Double totalAmount) { this.totalAmount = totalAmount; }
    public Double getTotalAmount() { return totalAmount; }

    /** @return the Vendor object assigned to this order */
    public User getVendor() { return vendor; }
    public void setVendor(User vendor) { this.vendor = vendor; }

    /** @return the original Purchase Request */
    public PurchaseRequest getPurchaseRequest() { return purchaseRequest; }
    public void setPurchaseRequest(PurchaseRequest purchaseRequest) { this.purchaseRequest = purchaseRequest; }

    /** @return the history of deliveries for this PO */
    public List<DeliveryRecord> getDeliveries() { return deliveries; }
    public void setDeliveries(List<DeliveryRecord> deliveries) { this.deliveries = deliveries; }

    public Integer getOrderedQuantity() { return orderedQuantity; }
    public void setOrderedQuantity(Integer orderedQuantity) { this.orderedQuantity = orderedQuantity; }

    public Double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(Double unitPrice) { this.unitPrice = unitPrice; }

    /** @return the sum to be paid after accounting for damaged goods */
    public Double getFinalPayableAmount() { return finalPayableAmount; }
    public void setFinalPayableAmount(Double finalPayableAmount) { this.finalPayableAmount = finalPayableAmount; }

    public String getVendorQuoteRef() { return vendorQuoteRef; }
    public void setVendorQuoteRef(String vendorQuoteRef) { this.vendorQuoteRef = vendorQuoteRef; }

    public String getPoNumber() { return poNumber; }
    public void setPoNumber(String poNumber) { this.poNumber = poNumber; }

    public String getSourcePr() { return sourcePr; }
    public void setSourcePr(String sourcePr) { this.sourcePr = sourcePr; }

    public String getQuoteRef() { return quoteRef; }
    public void setQuoteRef(String quoteRef) { this.quoteRef = quoteRef; }
}