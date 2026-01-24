package com.example.demo.Service;

import com.example.demo.Repository.ComplianceHistoryRepository;
import com.example.demo.Repository.DocumentRepository;
import com.example.demo.models.ComplianceHistory;
import com.example.demo.models.User;
import com.example.demo.models.VendorDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

/**
 * Service class handling the business logic for vendor document management.
 * This includes uploading files, retrieving binary data, and performing
 * compliance audits on submitted documents.
 */
@Service
public class DocumentService {

    @Autowired
    private DocumentRepository documentRepository;

    /**
     * Method: storeDocument
     * Working: Converts a web-uploaded MultipartFile into a database-persisted entity.
     * It extracts metadata like filename and content type before saving the raw bytes.
     * @param file The file received from the HTTP request.
     * @param vendor The User entity representing the owner of the document.
     * @return The saved VendorDocument entity.
     * @throws IOException If there is an error reading the file bytes.
     */
    public VendorDocument storeDocument(MultipartFile file, User vendor) throws IOException {
        VendorDocument doc = new VendorDocument();
        doc.setFileName(file.getOriginalFilename());
        doc.setFileType(file.getContentType());
        doc.setData(file.getBytes());
        doc.setVendor(vendor);

        return documentRepository.save(doc);
    }

    @Autowired
    private ComplianceHistoryRepository historyRepository; // You'll create this repository next

    /**
     * Method: verifyDocument
     * Working: Updates a document's status (APPROVED/REJECTED) and creates an
     * audit trail entry in the ComplianceHistory table.
     * @param docId The ID of the document being reviewed.
     * @param status The new status to be applied.
     * @param adminUsername The username of the administrator performing the action.
     * @param comments Optional feedback or reasons for the status change.
     */
    public void verifyDocument(Long docId, String status, String adminUsername, String comments) {
        VendorDocument doc = documentRepository.findById(docId)
                .orElseThrow(() -> new RuntimeException("Document not found"));

        // Create history record to maintain an audit trail
        ComplianceHistory history = new ComplianceHistory();
        history.setDocument(doc);
        history.setOldStatus(doc.getComplianceStatus());
        history.setNewStatus(status);
        history.setChangedBy(adminUsername);
        history.setComments(comments);

        // Update document status in the main document table
        doc.setComplianceStatus(status);

        documentRepository.save(doc);
        historyRepository.save(history);
    }

    /**
     * Retrieves a document's metadata and binary data by its unique ID.
     * @param id The primary key of the document.
     * @return The VendorDocument entity.
     * @throws RuntimeException If the document does not exist.
     */
    public VendorDocument getDocument(Long id) {
        return documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Document not found"));
    }
}