package com.bookmyticket.contact;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/contact")
public class ContactController {

    private final ContactMessageRepository repository;

    public ContactController(ContactMessageRepository repository) {
        this.repository = repository;
    }

    @PostMapping
    public ResponseEntity<ContactDtos.ContactResponse> submit(
            @Valid @RequestBody ContactDtos.ContactRequest request) {
        // Honeypot field: bots fill it, humans never see it. Accept silently.
        if (request.company() != null && !request.company().isBlank()) {
            return ResponseEntity.status(HttpStatus.CREATED).body(new ContactDtos.ContactResponse(null,
                    "RECEIVED", "Thanks — your message has been queued."));
        }
        ContactMessage message = new ContactMessage();
        message.setName(request.name().trim());
        message.setEmail(request.email().trim().toLowerCase());
        message.setSubject(request.subject().trim());
        message.setMessage(request.message().trim());
        message.setStatus("NEW");
        ContactMessage saved = repository.save(message);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ContactDtos.ContactResponse(saved.getId(),
                saved.getStatus(), "Thanks — the team will get back to you at " + saved.getEmail() + "."));
    }
}
