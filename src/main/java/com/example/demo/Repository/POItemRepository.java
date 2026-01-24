package com.example.demo.Repository;

import com.example.demo.models.POItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Interface: POItemRepository
 * Explanation: This interface provides all CRUD operations for the POItem entity.
 * It allows the POService to find items by ID and save updated delivery quantities.
 */
@Repository
public interface POItemRepository extends JpaRepository<POItem, Long> {
}