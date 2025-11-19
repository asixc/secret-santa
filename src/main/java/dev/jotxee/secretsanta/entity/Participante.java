package dev.jotxee.secretsanta.entity;

import dev.jotxee.secretsanta.util.EmailCryptoService;
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
    @Convert(converter = EmailEncryptConverter.class)
    private String email;
    
    @Column(length = 10)
    private String genero; // "hombre", "mujer", o null
    
    @Column(nullable = false, unique = true)
    private String token;
    
    @Column(name = "asignado_a")
    @Convert(converter = EmailEncryptConverter.class)
    private String asignadoA; // aquí se almacena el email cifrado del asignado
    
    // Campos para autenticación
    @Column
    private String password; // BCrypt hash

    @Column(nullable = false, length = 20)
    private String role = "USER"; // USER o ADMIN

    // Campos de tallas
    @Column(length = 10)
    private String tallaCamisa;

    @Column(length = 10)
    private String tallaPantalon;

    @Column(length = 10)
    private String tallaZapato;

    @Column(length = 10)
    private String tallaChaqueta;

    @Column(length = 1000)
    private String preferencias;

    @PrePersist
    protected void onCreate() {
        if (token == null) {
            token = UUID.randomUUID().toString();
        }
        if (role == null) {
            role = "USER";
        }
    }

    public String getAsignadoAEncrypted() {
        if (this.asignadoA == null || this.asignadoA.isEmpty()) return null;
        // Vuelve a cifrar el valor desencriptado para mostrarlo siempre cifrado
        // Usa la misma clave estática que el converter
        return new EmailCryptoService(
            EmailEncryptConverter.staticKey
        ).encrypt(this.getAsignadoA());
    }

    public String getEmailEncrypted() {
        if (this.email == null || this.email.isEmpty()) return null;
        return new EmailCryptoService(EmailEncryptConverter.staticKey).encrypt(this.getEmail());
    }
}
