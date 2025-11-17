package dev.jotxee.secretsanta.controller;

import dev.jotxee.secretsanta.event.SorteoCreatedEvent;
import dev.jotxee.secretsanta.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/test")
@RequiredArgsConstructor
@Slf4j
public class TestController {
    
    private final EmailService emailService;
    private final ApplicationEventPublisher eventPublisher;
    
    @GetMapping("/email")
    public String testEmail(@RequestParam String email) {
        log.info("🧪 Probando envío directo de email a: {}", email);
        
        SorteoCreatedEvent.ParticipantPayload testParticipant = 
            new SorteoCreatedEvent.ParticipantPayload(1L, "Test User", email, "Test Assigned", "test-token");
            
        emailService.sendParticipantEmail("Sorteo de Prueba", 20.0, 50.0, testParticipant);
        
        return "Email de prueba enviado a " + email;
    }
    
    @GetMapping("/event")
    public String testEvent(@RequestParam String email) {
        log.info("🧪 Probando publicación de evento para email: {}", email);
        
        SorteoCreatedEvent testEvent = new SorteoCreatedEvent(
            999L,
            "Sorteo de Prueba Evento",
            20.0,
            50.0,
            List.of(new SorteoCreatedEvent.ParticipantPayload(1L, "Test User", email, "Test Assigned", "test-token"))
        );
        
        eventPublisher.publishEvent(testEvent);
        
        return "Evento de prueba publicado para " + email;
    }
    
    @GetMapping("/event-transactional")
    @Transactional
    public String testEventTransactional(@RequestParam String email) {
        log.info("🧪 Probando publicación de evento TRANSACCIONAL para email: {}", email);
        
        SorteoCreatedEvent testEvent = new SorteoCreatedEvent(
            888L,
            "Sorteo de Prueba Transaccional",
            20.0,
            50.0,
            List.of(new SorteoCreatedEvent.ParticipantPayload(1L, "Test User", email, "Test Assigned", "test-token"))
        );
        
        eventPublisher.publishEvent(testEvent);
        log.info("✅ Evento transaccional publicado, esperando commit...");
        
        return "Evento transaccional publicado para " + email;
    }
}
