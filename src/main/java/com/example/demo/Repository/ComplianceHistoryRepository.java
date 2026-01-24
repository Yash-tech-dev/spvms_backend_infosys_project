package com.example.demo.Repository;

import com.example.demo.models.ComplianceHistory; // Check this import!
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for ComplianceHistory entity.
 * Provides the abstraction layer for database operations related to
 * the compliance audit trail.
 */
@Repository
public interface ComplianceHistoryRepository extends JpaRepository<ComplianceHistory, Long> {
    // No code needed here, save() is inherited automatically
}