package dev.jotxee.secretsanta.dto;

import lombok.Data;

@Data
public class ParticipanteFormDTO {
    private String nombre;
    private String email;
    private String genero; // "hombre" o "mujer"
}
