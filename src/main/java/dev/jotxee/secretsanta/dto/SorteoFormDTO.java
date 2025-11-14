package dev.jotxee.secretsanta.dto;

import lombok.Data;

import java.util.List;

@Data
public class SorteoFormDTO {
    private String nombre;
    private List<ParticipanteFormDTO> participantes;
}
