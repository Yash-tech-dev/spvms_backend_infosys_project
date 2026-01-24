package com.example.demo.models;

//We need a table to keep track of who uploaded what. We will store the file metadata (name, type) in the database and the actual bytes as a BLOB (Binary Large Object). This keeps everything inside your MySQL database for now, which is easier for a demo.
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity representing documents uploaded by vendors (e.g., certificates, licenses).
 * This class stores both the metadata and the actual binary content of the file
 * for compliance verification.
 */
@Entity
@Table(name = "vendor_documents")
public class VendorDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The original name of the uploaded file (e.g., "tax_certificate.pdf"). */
    private String fileName;

    /** The MIME type of the file (e.g., "application/pdf" or "image/png"). */
    private String fileType;

    /** * The actual binary content of the file.
     * @Lob indicates a Large Object, and LONGBLOB allows files up to 4GB in MySQL.
     */
    @Lob
    @Basic(fetch = FetchType.EAGER)
    @Column(columnDefinition = "LONGBLOB")
    private byte[] data;

    /** Current validation state (PENDING, APPROVED, or REJECTED). Defaults to PENDING. */
    private String complianceStatus = "PENDING"; // PENDING, APPROVED, REJECTED

    /** The exact timestamp when the document was stored in the system. */
    private LocalDateTime uploadDate = LocalDateTime.now();

    /** * The User (with ROLE_VENDOR) who owns this document.
     * Establishes a many-to-one relationship with the User table.
     */
    @ManyToOne
    @JoinColumn(name = "vendor_id")
    private User vendor; // Links the document to the user who uploaded it

    // Getters and Setters
    // Standard Getters and Setters

    /** @return the unique document ID */
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    /** @return the stored file name */
    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /** @return the file extension or MIME type */
    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    /** @return the raw byte array of the file */
    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    /** @return the current status of the document review */
    public String getComplianceStatus() {
        return complianceStatus;
    }

    public void setComplianceStatus(String complianceStatus) {
        this.complianceStatus = complianceStatus;
    }

    /** @return the date the file was uploaded */
    public LocalDateTime getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(LocalDateTime uploadDate) {
        this.uploadDate = uploadDate;
    }

    /** @return the User object representing the vendor */
    public User getVendor() {
        return vendor;
    }

    public void setVendor(User vendor) {
        this.vendor = vendor;
    }
}