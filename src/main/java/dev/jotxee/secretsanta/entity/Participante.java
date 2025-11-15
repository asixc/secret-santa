package dev.jotxee.secretsanta.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "participantes",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"sorteo_id", "email"}, name = "uk_sorteo_email"),
        @UniqueConstraint(columnNames = {"token"}, name = "uk_token")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Participante {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sorteo_id", nullable = false)
    private Sorteo sorteo;
    
    @Column(nullable = false)
    private String nombre;
    
    @Column(nullable = false)
    private String email;
    
    @Column(length = 10)
    private String genero; // "hombre", "mujer", o null
    
    @Column(nullable = false, unique = true)
    private String token;
    
    @Column(name = "asignado_a")
    private String asignadoA;
    
    @PrePersist
    protected void onCreate() {
        if (token == null) {
            token = UUID.randomUUID().toString();
        }
    }
}
