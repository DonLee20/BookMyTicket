package com.bookmyticket.contact;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class ContactDtos {

    private ContactDtos() {
    }

    public record ContactRequest(
            @NotBlank @Size(max = 120) String name,
            @NotBlank @Email @Size(max = 190) String email,
            @NotBlank @Size(max = 200) String subject,
            @NotBlank @Size(min = 10, max = 4000, message = "Please describe your query in at least 10 characters.") String message,
            @Size(max = 200) String company
    ) {
    }

    public record ContactResponse(Long id, String status, String message) {
    }
}
