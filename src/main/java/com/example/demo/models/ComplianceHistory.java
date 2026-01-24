package com.example.demo.models;

// usecase;-  "Add compliance status history table," we need to track every time an Admin changes a document from PENDING to APPROVED or REJECTED.
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity representing the audit trail for document compliance.
 * It records every status transition (e.g., PENDING to APPROVED)
 * performed by an administrator for auditing purposes.
 */
@Entity
@Table(name = "compliance_history")
public class ComplianceHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The document associated with this specific history record.
     * Links this audit entry to the VendorDocument being reviewed.
     */
    @ManyToOne
    @JoinColumn(name = "document_id")
    private VendorDocument document;

    /** The status of the document before the change occurred. */
    private String oldStatus;

    /** The status of the document after the admin's action. */
    private String newStatus;

    /** The username of the Administrator who performed the status update. */
    private String changedBy; // The Admin username

    /** Optional feedback or reasons for approval/rejection. */
    private String comments;

    /** The exact timestamp when the status change was recorded. */
    private LocalDateTime changeDate = LocalDateTime.now();

    // --- GETTERS AND SETTERS ---

    /** @return the unique ID of the history record */
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    /** @return the document associated with this audit entry */
    public VendorDocument getDocument() {
        return document;
    }

    public void setDocument(VendorDocument document) {
        this.document = document;
    }

    /** @return the previous status string */
    public String getOldStatus() {
        return oldStatus;
    }

    public void setOldStatus(String oldStatus) {
        this.oldStatus = oldStatus;
    }

    /** @return the updated status string */
    public String getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(String newStatus) {
        this.newStatus = newStatus;
    }

    /** @return the admin username who made the change */
    public String getChangedBy() {
        return changedBy;
    }

    public void setChangedBy(String changedBy) {
        this.changedBy = changedBy;
    }

    /** @return the remarks or comments regarding the change */
    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    /** @return the date and time of the change */
    public LocalDateTime getChangeDate() {
        return changeDate;
    }

    public void setChangeDate(LocalDateTime changeDate) {
        this.changeDate = changeDate;
    }
}