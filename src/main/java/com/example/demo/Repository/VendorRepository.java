package com.example.demo.Repository;

import com.example.demo.models.Vendor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Data Access Layer for the Vendor entity.
 * This repository provides the interface for performing CRUD operations
 * (Create, Read, Update, Delete) on Vendor data in the database.
 */
@Repository
public interface VendorRepository extends JpaRepository<Vendor, Long> {
}