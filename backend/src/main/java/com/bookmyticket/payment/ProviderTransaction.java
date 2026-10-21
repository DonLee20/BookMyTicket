package com.bookmyticket.payment;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/** Observability record for provider calls. Never stores credentials or PII. */
@Entity
@Table(name = "provider_transactions")
@Getter
@Setter
@NoArgsConstructor
public class ProviderTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 40)
    private String provider;

    @Column(nullable = false, length = 20)
    private String category;

    @Column(nullable = false, length = 40)
    private String operation;

    @Column(length = 100)
    private String reference;

    @Column(nullable = false)
    private boolean success;

    @Column(name = "latency_ms")
    private Integer latencyMs;

    @Column(name = "error_code", length = 60)
    private String errorCode;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
