package com.bookmyticket.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

/**
 * Supports "migrate only" runs (bun run db:migrate): Flyway executes before
 * runners, then the process exits without opening a web server.
 * Activated with APP_MIGRATE_ONLY=true + SPRING_MAIN_WEB_APPLICATION_TYPE=none.
 */
@Component
@ConditionalOnProperty(name = "app.migrate-only", havingValue = "true")
public class MigrationModeRunner implements ApplicationRunner {

    private final ConfigurableApplicationContext context;

    public MigrationModeRunner(ConfigurableApplicationContext context) {
        this.context = context;
    }

    @Override
    public void run(ApplicationArguments args) {
        int code = SpringApplication.exit(context, () -> 0);
        System.exit(code);
    }
}
