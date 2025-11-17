package dev.jotxee.secretsanta.service;

import dev.jotxee.secretsanta.dto.RevealResponse;
import dev.jotxee.secretsanta.entity.Participante;
import dev.jotxee.secretsanta.entity.Sorteo;
import dev.jotxee.secretsanta.event.ReenvioEmailParticipanteEvent;
import dev.jotxee.secretsanta.event.SorteoCreatedEvent;
import dev.jotxee.secretsanta.repository.ParticipanteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SecretSantaService {
    
    private final ParticipanteRepository participanteRepository;
    private final ApplicationEventPublisher eventPublisher;
    
    @Transactional(readOnly = true)
    public RevealResponse getRevealData(String token) {
        Participante participante = participanteRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Token inválido o no encontrado"));

        // Obtener todos los participantes del sorteo
        List<Participante> participantes = participanteRepository.findBySorteoId(participante.getSorteo().getId());

        // Desencriptar el email asignado
        String asignadoAEmail = participante.getAsignadoA();
        String nombreAsignado = participantes.stream()
                .filter(p -> p.getEmail().equalsIgnoreCase(asignadoAEmail))
                .map(Participante::getNombre)
                .findFirst()
                .orElse("(desconocido)");

        List<String> allNames = participantes.stream().map(Participante::getNombre).toList();

        return new RevealResponse(
            allNames,
            nombreAsignado,
            participante.getNombre(),
            participante.getGenero(),
            participante.getSorteo().getNombre(),
            participante.getSorteo().getImporteMinimo(),
            participante.getSorteo().getImporteMaximo()
        );
    }
    
    @Transactional
    public void reenviarEmailParticipante(Long participanteId) {
        Participante participante = participanteRepository.findById(participanteId)
            .orElseThrow(() -> new RuntimeException("Participante no encontrado"));
        Sorteo sorteo = participante.getSorteo();
        SorteoCreatedEvent.ParticipantPayload payload = new SorteoCreatedEvent.ParticipantPayload(
            participante.getId(),
            participante.getNombre(),
            participante.getEmail(),
            participante.getAsignadoA(),
            participante.getToken()
        );
        ReenvioEmailParticipanteEvent evento = new ReenvioEmailParticipanteEvent(
            sorteo.getNombre(),
            sorteo.getImporteMinimo(),
            sorteo.getImporteMaximo(),
            payload
        );
        eventPublisher.publishEvent(evento);
    }
}
