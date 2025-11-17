package dev.jotxee.secretsanta.controller;

import dev.jotxee.secretsanta.dto.RevealResponse;
import dev.jotxee.secretsanta.entity.Participante;
import dev.jotxee.secretsanta.event.SorteoCreatedEvent;
import dev.jotxee.secretsanta.repository.ParticipanteRepository;
import dev.jotxee.secretsanta.service.EmailService;
import dev.jotxee.secretsanta.service.SecretSantaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ApiController {
    
    private final SecretSantaService secretSantaService;
    private final ParticipanteRepository participanteRepository;
    private final EmailService emailService;
    
    @GetMapping("/reveal")
    public ResponseEntity<RevealResponse> reveal(@RequestParam String token) {
        try {
            RevealResponse response = secretSantaService.getRevealData(token);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PostMapping("/participante/{id}/resend")
    public ResponseEntity<Void> resendEmail(@PathVariable Long id) {
        try {
            Participante participante = participanteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Participante no encontrado"));
            
            var payload = new SorteoCreatedEvent.ParticipantPayload(
                participante.getId(),
                participante.getNombre(),
                participante.getEmail(),
                participante.getAsignadoA(),
                participante.getToken()
            );
            
            emailService.sendParticipantEmail(
                participante.getSorteo().getNombre(),
                participante.getSorteo().getImporteMinimo(),
                participante.getSorteo().getImporteMaximo(),
                payload
            );
            
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PutMapping("/participante/{id}/email")
    public ResponseEntity<Void> updateEmail(@PathVariable Long id, @RequestBody Map<String, String> body) {
        try {
            String newEmail = body.get("email");
            if (newEmail == null || newEmail.isBlank()) {
                return ResponseEntity.badRequest().build();
            }
            
            Participante participante = participanteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Participante no encontrado"));
            
            participante.setEmail(newEmail);
            participanteRepository.save(participante);
            
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
