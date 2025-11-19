package dev.jotxee.secretsanta.service;

import dev.jotxee.secretsanta.dto.RevealResponse;
import dev.jotxee.secretsanta.entity.PerfilSorteo;
import dev.jotxee.secretsanta.entity.Sorteo;
import dev.jotxee.secretsanta.event.ReenvioEmailParticipanteEvent;
import dev.jotxee.secretsanta.event.SorteoCreatedEvent;
import dev.jotxee.secretsanta.repository.PerfilSorteoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SecretSantaService {
    
    private final PerfilSorteoRepository perfilSorteoRepository;
    private final ApplicationEventPublisher eventPublisher;
    
    @Transactional(readOnly = true)
    public RevealResponse getRevealData(String token) {
        PerfilSorteo perfil = perfilSorteoRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Token inválido o no encontrado"));

        // Obtener todos los perfiles del sorteo
        List<PerfilSorteo> perfiles = perfilSorteoRepository.findBySorteoIdWithUsuario(perfil.getSorteo().getId());

        // Desencriptar el email asignado
        String asignadoAEmail = perfil.getAsignadoA();
        String nombreAsignado = perfiles.stream()
                .filter(p -> p.getUsuario().getEmail().equalsIgnoreCase(asignadoAEmail))
                .map(p -> p.getUsuario().getNombre())
                .findFirst()
                .orElse("(desconocido)");

        List<String> allNames = perfiles.stream()
                .map(p -> p.getUsuario().getNombre())
                .toList();

        return new RevealResponse(
            allNames,
            nombreAsignado,
            perfil.getUsuario().getNombre(),
            perfil.getUsuario().getGenero(),
            perfil.getSorteo().getNombre(),
            perfil.getSorteo().getImporteMinimo(),
            perfil.getSorteo().getImporteMaximo()
        );
    }
    
    @Transactional
    public void reenviarEmailParticipante(Long perfilId) {
        PerfilSorteo perfil = perfilSorteoRepository.findById(perfilId)
            .orElseThrow(() -> new RuntimeException("Perfil no encontrado"));
        Sorteo sorteo = perfil.getSorteo();
        SorteoCreatedEvent.ParticipantPayload payload = new SorteoCreatedEvent.ParticipantPayload(
            perfil.getId(),
            perfil.getUsuario().getNombre(),
            perfil.getUsuario().getEmail(),
            perfil.getAsignadoA(),
            perfil.getToken()
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
