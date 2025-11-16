package dev.jotxee.secretsanta.event;

import java.util.List;

/**
 * Evento de dominio que representa la creación de un sorteo y sus participantes.
 * Se serializa en un payload ligero para evitar exponer entidades JPA a otros componentes.
 */
public record SorteoCreatedEvent(
        Long sorteoId,
        String sorteoName,
        List<ParticipantPayload> participants
) {

    public record ParticipantPayload(
            Long id,
            String name,
            String email,
            String assignedTo,
            String token
    ) {}
}
