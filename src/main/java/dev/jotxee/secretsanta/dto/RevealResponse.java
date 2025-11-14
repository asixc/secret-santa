package dev.jotxee.secretsanta.dto;

import java.util.List;

public record RevealResponse(
    List<String> names,
    String assigned
) {}
