package dev.jotxee.secretsanta.config;

import javax.sql.DataSource;

import org.flywaydb.core.Flyway;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import lombok.extern.slf4j.Slf4j;

/**
 * Configuración manual de Flyway para Spring Boot 4.0.0-SNAPSHOT.
 * 
 * Spring Boot 4.0.0-SNAPSHOT parece no auto-configurar Flyway correctamente,
 * por lo que creamos un bean manual que se ejecuta antes de Hibernate.
 */
@Slf4j
@Configuration
@ConditionalOnClass(Flyway.class)
@ConditionalOnProperty(prefix = "spring.flyway", name = "enabled", havingValue = "true", matchIfMissing = false)
public class FlywayConfiguration {

    /**
     * Bean de Flyway que se ejecuta automáticamente al arrancar.
     * Spring invocará flyway.migrate() porque Flyway implementa InitializingBean.
     */
    @Bean
    @Primary
    public Flyway flyway(DataSource dataSource) {
        log.info("═══════════════════════════════════════════════════════");
        log.info("🔧 CONFIGURANDO FLYWAY MANUALMENTE");
        log.info("═══════════════════════════════════════════════════════");
        
        Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration")
                .baselineOnMigrate(true)
                .baselineVersion("1")
                .validateOnMigrate(true)
                .load();
        
        log.info("📍 Locations: {}", (Object[]) flyway.getConfiguration().getLocations());
        log.info("🔄 Baseline on migrate: {}", flyway.getConfiguration().isBaselineOnMigrate());
        log.info("📊 Baseline version: {}", flyway.getConfiguration().getBaselineVersion());
        log.info("🚀 Ejecutando migraciones...");
        
        try {
            var result = flyway.migrate();
            log.info("✅ FLYWAY COMPLETADO");
            log.info("   - Migraciones ejecutadas: {}", result.migrationsExecuted);
            log.info("   - Schema version: {}", result.targetSchemaVersion);
            log.info("   - Success: {}", result.success);
            log.info("═══════════════════════════════════════════════════════");
        } catch (Exception e) {
            log.error("❌ ERROR EN FLYWAY", e);
            throw e;
        }
        
        return flyway;
    }
}
