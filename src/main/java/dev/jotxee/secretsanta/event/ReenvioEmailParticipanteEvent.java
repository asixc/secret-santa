package dev.jotxee.secretsanta.event;

/**
 * Evento para el reenvío de email a un participante concreto.
 */
public record ReenvioEmailParticipanteEvent(
    String sorteoName,
    Double importeMinimo,
    Double importeMaximo,
    SorteoCreatedEvent.ParticipantPayload participant
) {}
