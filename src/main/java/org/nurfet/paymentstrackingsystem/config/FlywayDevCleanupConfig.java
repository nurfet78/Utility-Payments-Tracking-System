package org.nurfet.paymentstrackingsystem.config;

import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;

@Configuration
@Profile("dev")
public class FlywayDevCleanupConfig {

    private static final Logger log = LoggerFactory.getLogger(FlywayDevCleanupConfig.class);

    private final Flyway flyway;

    public FlywayDevCleanupConfig(Flyway flyway) {
        this.flyway = flyway;
        log.warn("╔════════════════════════════════════════════════════════════╗");
        log.warn("║  DEV MODE: База данных будет ОЧИЩЕНА при завершении!      ║");
        log.warn("║  Не используйте профиль 'dev' в production!               ║");
        log.warn("╚════════════════════════════════════════════════════════════╝");
    }

    /**
     * При завершении Spring Context вызывает flyway.clean().
     * Это удаляет ВСЕ объекты из схемы (таблицы, индексы, constraints).
     */
    @EventListener(ContextClosedEvent.class)
    public void onShutdown() {
        log.info("Очистка базы данных (dev profile)...");
        try {
            flyway.clean();
            log.info("База данных очищена.");
        } catch (Exception e) {
            log.error("Ошибка при очистке БД: {}", e.getMessage());
        }
    }
}
