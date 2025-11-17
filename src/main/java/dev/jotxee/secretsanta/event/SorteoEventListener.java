package dev.jotxee.secretsanta.event;

import dev.jotxee.secretsanta.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SorteoEventListener {

    private final EmailService emailService;

    @EventListener
    public void handleSorteoCreated(SorteoCreatedEvent event) {
        log.info("🎯 SorteoEventListener invocado - Procesando evento de sorteo creado {} con {} participantes",
                event.sorteoId(), event.participants().size());
        
        try {
            log.debug("📧 Iniciando envío de emails a participantes de forma asíncrona");
            event.participants()
                    .forEach(participant -> {
                        log.debug("📤 Enviando email a: {}", participant.email());
                        emailService.sendParticipantEmail(
                            event.sorteoName(), 
                            event.importeMinimo(),
                            event.importeMaximo(),
                            participant
                        );
                    });
            log.info("✅ Procesamiento del evento completado exitosamente");
        } catch (Exception e) {
            log.error("❌ Error procesando evento de sorteo creado", e);
            throw e;
        }
    }
}
