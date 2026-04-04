package com.greenleaf.paperplate.controller;

import com.greenleaf.paperplate.dto.ContactRequest;
import com.greenleaf.paperplate.model.Contact;
import com.greenleaf.paperplate.service.ContactService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // restrict to your domain in production
public class ContactController {

    private final ContactService contactService;

    /**
     * POST /api/contact
     * Accepts contact form submission, saves to DB, sends emails.
     */
    @PostMapping("/contact")
    public ResponseEntity<?> submitContact(@Valid @RequestBody ContactRequest request) {
        log.info("Received contact form from email={}", request.getEmail());
        Contact saved = contactService.saveAndNotify(request);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Thank you! We'll get back to you soon.",
                "id", saved.getId()
        ));
    }

    /** Health check endpoint for AWS load balancer */
    @GetMapping("/health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok(Map.of("status", "UP"));
    }
}
