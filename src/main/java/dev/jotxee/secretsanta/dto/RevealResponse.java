package dev.jotxee.secretsanta.dto;

import java.util.List;

public record RevealResponse(
    List<String> names,
    String assigned,
    String participantName,
    String gender,  // "hombre", "mujer", o null
    String sorteoName,
    Double importeMinimo,
    Double importeMaximo
) {}
