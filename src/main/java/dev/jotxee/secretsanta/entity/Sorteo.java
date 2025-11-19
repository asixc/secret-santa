package dev.jotxee.secretsanta.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "sorteos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Sorteo {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String nombre;
    
    @Column(name = "nombre_interno")
    private String nombreInterno;
    
    @Column(name = "importe_minimo")
    private Double importeMinimo;
    
    @Column(name = "importe_maximo")
    private Double importeMaximo;
    
    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;
    
    @Column(nullable = false)
    private Boolean activo = true;
    
    @OneToMany(mappedBy = "sorteo", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PerfilSorteo> perfiles = new ArrayList<>();
    
    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
    }
}
