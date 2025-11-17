package dev.jotxee.secretsanta.dto;

import lombok.Data;

import java.util.List;

@Data
public class SorteoFormDTO {
    private String nombre;
    private String nombreInterno;
    private Double importeMinimo;
    private Double importeMaximo;
    private List<ParticipanteFormDTO> participantes;
}
