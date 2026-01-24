package com.example.demo.Repository;

import com.example.demo.models.DeliveryRecord;
import com.example.demo.models.PurchaseOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Repository interface for DeliveryRecord entities.
 * Acts as the Data Access Layer (DAL) to perform database operations
 * on delivery logs.
 */
@Repository
public interface DeliveryRepository extends JpaRepository<DeliveryRecord, Long> {

    /**
     * Method: findByPurchaseOrder
     * Working: This is a derived query method. Hibernate will automatically
     * generate the SQL to find all delivery rows where po_id matches.
     * * @param purchaseOrder The PurchaseOrder entity used as a search filter.
     * @return A list of delivery records associated with the given PO.
     */
    List<DeliveryRecord> findByPurchaseOrder(PurchaseOrder purchaseOrder);
}