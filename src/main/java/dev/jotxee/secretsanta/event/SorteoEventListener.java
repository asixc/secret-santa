package dev.jotxee.secretsanta.event;

import dev.jotxee.secretsanta.service.EmailService;
import dev.jotxee.secretsanta.util.EmailCryptoService;
import dev.jotxee.secretsanta.entity.EmailEncryptConverter;
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
                        log.debug("📤 Enviando email a: {}", new EmailCryptoService(EmailEncryptConverter.staticKey).encrypt(participant.email()));
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

    @EventListener
    public void handleReenvioEmailParticipante(ReenvioEmailParticipanteEvent event) {
        log.info("🔄 Reenvío de email solicitado para participante {} en sorteo '{}'", new EmailCryptoService(EmailEncryptConverter.staticKey).encrypt(event.participant().email()), event.sorteoName());
        try {
            emailService.sendParticipantEmail(
                event.sorteoName(),
                event.importeMinimo(),
                event.importeMaximo(),
                event.participant()
            );
            log.info("✅ Email reenviado correctamente a {}", new EmailCryptoService(EmailEncryptConverter.staticKey).encrypt(event.participant().email()));
        } catch (Exception e) {
            log.error("❌ Error reenviando email a {}", new EmailCryptoService(EmailEncryptConverter.staticKey).encrypt(event.participant().email()), e);
            throw e;
        }
    }
}
