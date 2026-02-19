package com.example.demo.Repository;

import com.example.demo.models.POStatus;
import com.example.demo.models.PurchaseOrder;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Data Access Layer for PurchaseOrder entities.
 * This repository handles standard CRUD operations, complex JPA queries,
 * and calls to Database Stored Procedures for analytical reporting.
 */
@Repository
public interface PORepository extends JpaRepository<PurchaseOrder, Long> {

    /**
     * Retrieves all Purchase Orders assigned to a specific vendor.
     * @param vendorId The unique ID of the vendor.
     * @return A list of Purchase Orders for the specified vendor.
     */
    List<PurchaseOrder> findByVendorId(Long vendorId);

    /**
     * Executes a Stored Procedure to generate a monthly spending report.
     * @return A list of object arrays containing monthly financial data.
     */
    @Procedure(procedureName = "GetMonthlySpending")
    List<Object[]> getMonthlySpendingReport();

    /**
     * Executes a Stored Procedure to analyze vendor performance metrics.
     * @return A list of object arrays with vendor efficiency data.
     */
    @Procedure(procedureName = "GetVendorPerformance")
    List<Object[]> getVendorPerformanceReport();

    /**
     * Calls a native MySQL stored procedure to fetch the PR approval timeline.
     * @return Analytical data regarding the time taken for approvals.
     */
    @Query(value = "CALL GetPRApprovalTimeline()", nativeQuery = true)
    List<Object[]> getPRApprovalReport();

    /**
     * Executes a Stored Procedure to calculate order fulfillment rates.
     * @return Fulfillment statistics (Ordered vs. Delivered).
     */
    @Procedure(procedureName = "GetOrderFulfillmentRate")
    List<Object[]> getFulfillmentReport();


    /**
     * Counts Purchase Orders that match a specific list of active statuses.
     * @param statuses A list of POStatus enums to include in the count.
     * @return Total count of orders matching the provided statuses.
     */
    @Query("SELECT COUNT(p) FROM PurchaseOrder p WHERE p.status IN :statuses")
    long countByActiveStatuses(@Param("statuses") List<POStatus> statuses);


    /**
     * Counts all orders that are currently active (not Completed or Cancelled).
     * @return Total count of ongoing/active orders.
     */
    @Query("SELECT COUNT(p) FROM PurchaseOrder p WHERE p.status NOT IN (com.example.demo.models.POStatus.COMPLETED, com.example.demo.models.POStatus.CANCELLED)")
    long countAllActivePOs();

    /**
     * Counts all orders that have not been explicitly cancelled.
     * @return Count of all non-cancelled orders.
     */
    @Query("SELECT COUNT(p) FROM PurchaseOrder p WHERE p.status != com.example.demo.models.POStatus.CANCELLED")
    long countAllActiveOrders();

    /**
     * Counts orders based on a single specific status.
     * @param status The POStatus to filter by.
     * @return The count of orders with that status.
     */
    long countByStatus(POStatus status);

    /**
     * Counts orders while excluding a specific list of statuses.
     * @param excludedStatuses List of statuses to ignore in the count.
     * @return Count of orders whose status is not in the excluded list.
     */
    @Query("SELECT COUNT(p) FROM PurchaseOrder p WHERE p.status NOT IN :excludedStatuses")
    long countByStatusNotIn(@Param("excludedStatuses") List<POStatus> excludedStatuses);

    List<PurchaseOrder> findByCreatedByUsername(String username);

}