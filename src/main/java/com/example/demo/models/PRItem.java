package com.example.demo.models;
import com.fasterxml.jackson.annotation.JsonIgnore; // 1. Add this import

import jakarta.persistence.*;

/**
 * Entity representing an individual item within a Purchase Request (PR).
 * This class stores item details like name, quantity, and pricing,
 * and maintains a relationship with the parent PurchaseRequest.
 */
@Entity
public class PRItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String itemName;
    private Integer quantity;
    private Double unitPrice;
    private Double totalPrice;

    /**
     * Relationship mapping back to the parent PurchaseRequest.
     * Uses @JsonIgnore to prevent infinite recursion during JSON serialization.
     */
    @ManyToOne
    @JoinColumn(name = "pr_id")
    @JsonIgnore // 2. Add this annotation here to stop the infinite loop
    private PurchaseRequest purchaseRequest;

    // --- GETTERS AND SETTERS (Add these to fix the error) ---

    /**
     * Gets the quantity of the item requested.
     * @return the item quantity.
     */
    public Integer getQuantity() {
        return quantity;
    }

    /**
     * Sets the quantity of the item.
     * @param quantity the number of units.
     */
    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    /**
     * Gets the price per single unit of the item.
     * @return the unit price.
     */
    public Double getUnitPrice() {
        return unitPrice;
    }

    /**
     * Sets the price per single unit.
     * @param unitPrice the cost per unit.
     */
    public void setUnitPrice(Double unitPrice) {
        this.unitPrice = unitPrice;
    }

    /**
     * Gets the name or description of the item.
     * @return the item name.
     */
    public String getItemName() {
        return itemName;
    }

    /**
     * Sets the name or description of the item.
     * @param itemName the name of the item.
     */
    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    /**
     * Gets the calculated total price (Quantity * Unit Price).
     * @return the total price for this line item.
     */
    public Double getTotalPrice() {
        return totalPrice;
    }

    /**
     * Sets the total price for the item.
     * @param totalPrice the total cost.
     */
    public void setTotalPrice(Double totalPrice) {
        this.totalPrice = totalPrice;
    }

    /**
     * Retrieves the parent Purchase Request this item belongs to.
     * @return the associated PurchaseRequest object.
     */
    public PurchaseRequest getPurchaseRequest() {
        return purchaseRequest;
    }

    /**
     * Links this item to a specific Purchase Request.
     * @param purchaseRequest the parent PR.
     */
    public void setPurchaseRequest(PurchaseRequest purchaseRequest) {
        this.purchaseRequest = purchaseRequest;
    }

    /**
     * Gets the unique database identifier for this PR item.
     * @return the primary key ID.
     */
    public Long getId() {
        return id;
    }
}