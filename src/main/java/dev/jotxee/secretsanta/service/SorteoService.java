package dev.jotxee.secretsanta.service;

import dev.jotxee.secretsanta.dto.SorteoFormDTO;
import dev.jotxee.secretsanta.entity.Participante;
import dev.jotxee.secretsanta.entity.Sorteo;
import dev.jotxee.secretsanta.event.SorteoCreatedEvent;
import dev.jotxee.secretsanta.repository.ParticipanteRepository;
import dev.jotxee.secretsanta.repository.SorteoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SorteoService {

    private final SorteoRepository sorteoRepository;
    private final ParticipanteRepository participanteRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * Crea un sorteo completo con sus participantes y asignaciones de amigo invisible.
     * 
     * @param sorteoForm Formulario con los datos del sorteo y participantes
     * @return El sorteo creado
     * @throws IllegalArgumentException si hay menos de 3 participantes
     */
    @Transactional
    public Sorteo crearSorteo(SorteoFormDTO sorteoForm) {
        log.info("Iniciando creación de sorteo: {} con {} participantes", 
                sorteoForm.getNombre(), sorteoForm.getParticipantes().size());

        // Validaciones
        validarSorteo(sorteoForm);

        // Crear participantes en memoria
        List<Participante> participantes = crearParticipantesDesdeFormulario(sorteoForm);

        // Asignar amigos invisibles
        asignarAmigosInvisibles(participantes);
        log.info("Asignaciones calculadas correctamente para {} participantes", participantes.size());

        // Guardar en base de datos
        Sorteo sorteo = guardarSorteoConParticipantes(sorteoForm.getNombre(), participantes);

        // Publicar evento
        publicarEventoSorteoCreado(sorteo, participantes);

        log.info("Sorteo creado exitosamente con ID: {}", sorteo.getId());
        return sorteo;
    }

    /**
     * Valida que el sorteo tenga los datos mínimos requeridos.
     */
    private void validarSorteo(SorteoFormDTO sorteoForm) {
        if (sorteoForm.getParticipantes() == null || sorteoForm.getParticipantes().isEmpty()) {
            throw new IllegalArgumentException("Debe añadir participantes al sorteo");
        }

        if (sorteoForm.getParticipantes().size() < 3) {
            throw new IllegalArgumentException("Se necesitan al menos 3 participantes para crear un sorteo");
        }
    }

    /**
     * Crea las entidades Participante desde el formulario.
     */
    private List<Participante> crearParticipantesDesdeFormulario(SorteoFormDTO sorteoForm) {
        List<Participante> participantes = new ArrayList<>();
        
        sorteoForm.getParticipantes().forEach(dto -> {
            Participante participante = new Participante();
            participante.setNombre(dto.getNombre().trim());
            participante.setEmail(dto.getEmail().trim());
            participante.setGenero(dto.getGenero());
            participante.setToken(UUID.randomUUID().toString());
            participantes.add(participante);
        });

        return participantes;
    }

    /**
     * Algoritmo para asignar amigos invisibles de forma aleatoria.
     * Garantiza que nadie se tenga a sí mismo.
     */
    private void asignarAmigosInvisibles(List<Participante> participantes) {
        int size = participantes.size();
        
        // Crear lista de índices para barajar
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            indices.add(i);
        }

        boolean asignacionValida;
        int intentos = 0;
        final int MAX_INTENTOS = 100;

        do {
            Collections.shuffle(indices, secureRandom);
            asignacionValida = true;

            // Verificar que nadie se tenga a sí mismo
            for (int i = 0; i < size; i++) {
                if (indices.get(i) == i) {
                    asignacionValida = false;
                    break;
                }
            }

            intentos++;
            if (intentos >= MAX_INTENTOS) {
                log.error("No se pudo generar una asignación válida después de {} intentos", MAX_INTENTOS);
                throw new RuntimeException("Error al generar asignaciones de amigo invisible");
            }
        } while (!asignacionValida);

        // Asignar los nombres según el orden barajado
        for (int i = 0; i < size; i++) {
            int asignadoIndex = indices.get(i);
            participantes.get(i).setAsignadoA(participantes.get(asignadoIndex).getNombre());
        }

        log.debug("Asignaciones generadas en {} intentos", intentos);
    }

    /**
     * Guarda el sorteo y sus participantes en la base de datos.
     */
    private Sorteo guardarSorteoConParticipantes(String nombreSorteo, List<Participante> participantes) {
        // Crear y guardar el sorteo
        Sorteo sorteo = new Sorteo();
        sorteo.setNombre(nombreSorteo);
        sorteo.setFechaCreacion(LocalDateTime.now());
        sorteo.setActivo(true);
        sorteo = sorteoRepository.save(sorteo);

        // Asignar el sorteo a todos los participantes
        for (Participante participante : participantes) {
            participante.setSorteo(sorteo);
        }

        // Guardar todos los participantes
        participanteRepository.saveAll(participantes);
        
        log.debug("Sorteo y participantes guardados en base de datos");
        return sorteo;
    }

    /**
     * Publica el evento de sorteo creado para que los listeners lo procesen.
     * Este evento se publica dentro de la transacción para que los listeners
     * transaccionales se ejecuten después del commit.
     */
    private void publicarEventoSorteoCreado(Sorteo sorteo, List<Participante> participantes) {
        log.info("🚀 Publicando evento SorteoCreatedEvent para sorteo ID: {}", sorteo.getId());
        
        SorteoCreatedEvent evento = new SorteoCreatedEvent(
            sorteo.getId(),
            sorteo.getNombre(),
            participantes.stream()
                    .map(p -> new SorteoCreatedEvent.ParticipantPayload(
                            p.getId(),
                            p.getNombre(),
                            p.getEmail(),
                            p.getAsignadoA(),
                            p.getToken()
                    ))
                    .toList()
        );
        
        eventPublisher.publishEvent(evento);
        log.debug("✅ Evento publicado correctamente");
    }
}
