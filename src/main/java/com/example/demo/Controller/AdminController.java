package com.example.demo.Controller;

import com.example.demo.Service.DocumentService;
import com.example.demo.models.VendorDocument;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/admin") // This matches the start of your URL
public class AdminController {
    @Autowired
    private DocumentService documentService;

    @GetMapping("/dashboard") // This matches the end: /api/admin/dashboard
    public String getDashboard() {
        return "SUCCESS: You have reached the Admin Dashboard!";
    }

    @CrossOrigin(origins = "http://localhost:3000")

    @PostMapping("/verify-doc/{id}")
    public ResponseEntity<String> verifyDoc(
            @PathVariable Long id,
            @RequestParam String status,
            @RequestParam String comments,
            Principal principal) {

        documentService.verifyDocument(id, status, principal.getName(), comments);
        return ResponseEntity.ok("Document status updated to: " + status);
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<byte[]> downloadDocument(@PathVariable Long id) {
        VendorDocument doc = documentService.getDocument(id);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + doc.getFileName() + "\"")
                .contentLength(doc.getData().length)
                .contentType(MediaType.parseMediaType(doc.getFileType()))
                .body(doc.getData());
    }

}