package dev.jotxee.secretsanta.service;

import dev.jotxee.secretsanta.dto.RevealResponse;
import dev.jotxee.secretsanta.entity.Participante;
import dev.jotxee.secretsanta.repository.ParticipanteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SecretSantaService {
    
    private final ParticipanteRepository participanteRepository;
    
    @Transactional(readOnly = true)
    public RevealResponse getRevealData(String token) {
        Participante participante = participanteRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Token inválido o no encontrado"));
        
        // Obtener todos los nombres del sorteo
        List<String> allNames = participanteRepository.findBySorteoId(participante.getSorteo().getId())
                .stream()
                .map(Participante::getNombre)
                .toList();
        
        return new RevealResponse(
            allNames, 
            participante.getAsignadoA(),
            participante.getNombre(),
            participante.getGenero(),
            participante.getSorteo().getNombre()
        );
    }
}
