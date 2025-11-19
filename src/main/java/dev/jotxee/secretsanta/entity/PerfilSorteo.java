package dev.jotxee.secretsanta.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "perfil_sorteo")
@Data
@NoArgsConstructor
public class PerfilSorteo {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sorteo_id", nullable = false)
    private Sorteo sorteo;
    
    @Column(unique = true)
    private String token;
    
    @Column(name = "asignado_a")
    @Convert(converter = EmailEncryptConverter.class)
    private String asignadoA;
    
    @Column(name = "talla_camisa")
    private String tallaCamisa;
    
    @Column(name = "talla_pantalon")
    private String tallaPantalon;
    
    @Column(name = "talla_zapato")
    private String tallaZapato;
    
    @Column(name = "talla_chaqueta")
    private String tallaChaqueta;
    
    @Column(columnDefinition = "TEXT")
    private String preferencias;
}
