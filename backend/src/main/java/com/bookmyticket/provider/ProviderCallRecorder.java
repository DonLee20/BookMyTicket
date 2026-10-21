package com.bookmyticket.provider;

import com.bookmyticket.payment.ProviderTransaction;
import com.bookmyticket.payment.ProviderTransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;

/** Records provider calls for observability. Never stores credentials or PII. */
@Service
public class ProviderCallRecorder {

    private final ProviderTransactionRepository repository;
    private final TransactionTemplate transactionTemplate;

    public ProviderCallRecorder(ProviderTransactionRepository repository,
                                PlatformTransactionManager transactionManager) {
        this.repository = repository;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
        // Always persist telemetry, even when the caller is inside a read-only search transaction.
        this.transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        this.transactionTemplate.setReadOnly(false);
    }

    public void record(String provider, String category, String operation, String reference,
                       boolean success, long latencyMs, String errorCode) {
        transactionTemplate.executeWithoutResult(status -> {
            ProviderTransaction tx = new ProviderTransaction();
            tx.setProvider(provider);
            tx.setCategory(category);
            tx.setOperation(operation);
            tx.setReference(reference);
            tx.setSuccess(success);
            tx.setLatencyMs((int) Math.min(latencyMs, Integer.MAX_VALUE));
            tx.setErrorCode(errorCode);
            tx.setCreatedAt(Instant.now());
            repository.save(tx);
        });
    }

    public <T> T timed(String provider, String category, String operation, String reference, java.util.function.Supplier<T> call) {
        long start = System.nanoTime();
        try {
            T result = call.get();
            record(provider, category, operation, reference, true, (System.nanoTime() - start) / 1_000_000, null);
            return result;
        } catch (RuntimeException ex) {
            record(provider, category, operation, reference, false, (System.nanoTime() - start) / 1_000_000,
                    ex.getClass().getSimpleName());
            throw ex;
        }
    }
}
