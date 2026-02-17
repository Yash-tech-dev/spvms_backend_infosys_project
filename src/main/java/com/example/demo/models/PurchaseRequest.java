package com.example.demo.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing a formal Purchase Request (PR).
 * This class captures the initial demand for goods or services, including
 * descriptions, estimated costs, and associated vendor quotations.
 */
@Entity
@Table(name = "purchase_request")
public class PurchaseRequest {

    /** The vendor suggested or assigned to this request. */
    @ManyToOne
    @JoinColumn(name = "vendor_id")
    private User vendor;

    /** Current processing status of the request (default is "PENDING"). */
    @Column(name = "status")
    private String status = "PENDING";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String description;

    /** The total financial estimation for the entire request. */
    private Double estimatedTotalCost = 0.0;

    /** * Link to PRItem: One PR can have many items.
     * Uses EAGER fetching to ensure items are loaded with the request.
     */
    @OneToMany(mappedBy = "purchaseRequest", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<PRItem> items = new ArrayList<>();


    /** * Link to Vendor Quote from Sprint 3 (Requirement 3).
     * Links to the digital document uploaded by the vendor.
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "quote_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "vendor"})
    private VendorDocument quote;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();


    @Column(name = "created_by_username")
    private String createdByUsername;
    /**
     * Method: calculateTotal (Requirement 5)
     * Working: Streams through all line items, calculates (Quantity * Price) for each,
     * and aggregates the sum into the estimatedTotalCost field.
     */
    public void calculateTotal() {
        this.estimatedTotalCost = items.stream()
                .mapToDouble(item -> item.getQuantity() * item.getUnitPrice())
                .sum();
    }



    /** @return the unique database ID for the request */
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    /** @return the textual description of the request */
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    /** @return the calculated estimated total cost */
    public Double getEstimatedTotalCost() { return estimatedTotalCost; }
    public void setEstimatedTotalCost(Double estimatedTotalCost) { this.estimatedTotalCost = estimatedTotalCost; }

    /** @return the list of individual items within this request */
    public List<PRItem> getItems() { return items; }
    public void setItems(List<PRItem> items) { this.items = items; }

    /** @return the linked quotation document */
    public VendorDocument getQuote() { return quote; }
    public void setQuote(VendorDocument quote) { this.quote = quote; }

    /** @return the timestamp of when the request was first created */
    public LocalDateTime getCreatedAt() { return createdAt; }

    /** @return the timestamp of the last update to this request */
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    /** @return the current status (e.g., PENDING, APPROVED) */
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    /** @return the User object acting as the vendor for this request */
    public User getVendor() {
        return vendor;
    }

    public void setVendor(User vendor) {
        this.vendor = vendor;
    }

    public String getCreatedByUsername() { return createdByUsername; }
    public void setCreatedByUsername(String createdByUsername) { this.createdByUsername = createdByUsername; }
}