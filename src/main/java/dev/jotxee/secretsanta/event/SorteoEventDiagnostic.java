package dev.jotxee.secretsanta.event;

import dev.jotxee.secretsanta.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

// @Component  // Desactivado - solo para diagnóstico
// Testing with http://localhost:8080/test/event-transactional?email=joselitosbd@gmail.com
@RequiredArgsConstructor
@Slf4j
public class SorteoEventDiagnostic {

    private final EmailService emailService;

    @EventListener
    @Order(1)
    public void handleSorteoCreatedImmediate(SorteoCreatedEvent event) {
        log.info("🔴 IMMEDIATE LISTENER - Evento recibido inmediatamente para sorteo {} con {} participantes",
                event.sorteoId(), event.participants().size());
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    @Order(2)
    public void handleSorteoCreatedBeforeCommit(SorteoCreatedEvent event) {
        log.info("🟡 BEFORE_COMMIT LISTENER - Evento antes de commit para sorteo {} con {} participantes",
                event.sorteoId(), event.participants().size());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Order(3)
    public void handleSorteoCreatedAfterCommit(SorteoCreatedEvent event) {
        log.info("🟢 AFTER_COMMIT LISTENER - Evento después de commit para sorteo {} con {} participantes",
                event.sorteoId(), event.participants().size());
        
        // Solo enviamos UN email desde aquí para evitar duplicados
        if (!event.participants().isEmpty()) {
            var firstParticipant = event.participants().get(0);
            log.info("📧 Enviando email de prueba a primer participante: {}", firstParticipant.email());
            emailService.sendParticipantEmail(
                event.sorteoName() + " (AFTER_COMMIT)", 
                event.importeMinimo(),
                event.importeMaximo(),
                firstParticipant
            );
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
    @Order(4)
    public void handleSorteoCreatedAfterRollback(SorteoCreatedEvent event) {
        log.warn("🔴 AFTER_ROLLBACK LISTENER - Transacción falló para sorteo {} con {} participantes",
                event.sorteoId(), event.participants().size());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMPLETION)
    @Order(5)
    public void handleSorteoCreatedAfterCompletion(SorteoCreatedEvent event) {
        log.info("🏁 AFTER_COMPLETION LISTENER - Transacción completada para sorteo {} con {} participantes",
                event.sorteoId(), event.participants().size());
    }
}
