package com.example.demo.Repository;

import com.example.demo.models.VendorDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

/**
 * Data Access Layer for VendorDocument entities.
 * This interface handles all database interactions for uploaded files and metadata.
 */
public interface DocumentRepository extends JpaRepository<VendorDocument, Long> {

    /**
     * Method: findByVendorId
     * Working: This is a custom finder method using Spring Data JPA's method naming convention.
     * It allows the system to retrieve all documents belonging to a specific vendor
     * by filtering the 'vendor_id' foreign key in the database.
     * * @param vendorId The unique ID of the vendor whose documents are being requested.
     * @return A list of VendorDocument objects associated with that vendor.
     */
    List<VendorDocument> findByVendorId(Long vendorId);
}