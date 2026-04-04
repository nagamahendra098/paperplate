package com.greenleaf.paperplate.service;

import com.greenleaf.paperplate.dto.ContactRequest;
import com.greenleaf.paperplate.model.Contact;
import com.greenleaf.paperplate.repository.ContactRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContactService {

    private final ContactRepository contactRepository;
    private final EmailService emailService;

    public Contact saveAndNotify(ContactRequest req) {
        // 1. Persist to DB
        Contact contact = Contact.builder()
                .name(req.getName())
                .phone(req.getPhone())
                .email(req.getEmail())
                .location(req.getLocation())
                .message(req.getMessage())
                .build();

        Contact saved = contactRepository.save(contact);
        log.info("Saved contact id={} email={}", saved.getId(), saved.getEmail());

        // 2. Send thank-you email to customer (async)
        emailService.sendThankYouEmail(req.getEmail(), req.getName());

        // 3. Send internal notification to factory team (async)
        emailService.sendInternalNotification(
                req.getName(), req.getPhone(),
                req.getEmail(), req.getLocation(), req.getMessage()
        );

        return saved;
    }
}
