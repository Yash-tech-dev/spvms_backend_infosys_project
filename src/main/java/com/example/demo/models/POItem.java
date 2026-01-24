package com.example.demo.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import java.util.List;
import java.util.ArrayList;

@Entity
public class POItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String itemName;
    private Integer orderedQuantity;
    private Integer deliveredQuantity = 0;
    private Double unitPrice;

    @ManyToOne
    @JoinColumn(name = "po_id")
    @JsonBackReference(value = "po-items")
    private PurchaseOrder purchaseOrder;

    @OneToMany(mappedBy = "poItem", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference(value = "item-deliveries")
    private List<DeliveryRecord> deliveryHistory = new ArrayList<>();

    public POItem() {}

    // --- NEW METHOD ADDED ---
    /**
     * Method: setUnitPrice
     * Working: Allows Hibernate and the Service layer to assign a price to the item.
     * Without this, the field remains null, causing the delivery record to also show null.
     */
    public void setUnitPrice(Double unitPrice) {
        this.unitPrice = unitPrice;
    }

    // Inside POItem.java
    /**
     * Method: getTotalItemPrice
     * Working: Calculates the total cost for this specific item (e.g., 5 Laptops * 1000).
     */
    public Double getTotalItemPrice() {
        return (this.unitPrice != null && this.orderedQuantity != null)
                ? this.unitPrice * this.orderedQuantity : 0.0;
    }

    // --- EXISTING METHODS KEPT ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }

    public Integer getOrderedQuantity() { return orderedQuantity; }
    public void setOrderedQuantity(Integer orderedQuantity) { this.orderedQuantity = orderedQuantity; }

    public Integer getDeliveredQuantity() { return deliveredQuantity; }
    public void setDeliveredQuantity(Integer deliveredQuantity) { this.deliveredQuantity = deliveredQuantity; }

    public PurchaseOrder getPurchaseOrder() { return purchaseOrder; }
    public void setPurchaseOrder(PurchaseOrder purchaseOrder) { this.purchaseOrder = purchaseOrder; }

    public Double getUnitPrice() {
        return unitPrice;
    }

    public List<DeliveryRecord> getDeliveryHistory() { return deliveryHistory; }
    public void setDeliveryHistory(List<DeliveryRecord> deliveryHistory) { this.deliveryHistory = deliveryHistory; }}
//