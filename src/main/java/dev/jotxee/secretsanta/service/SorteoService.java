package dev.jotxee.secretsanta.service;

import dev.jotxee.secretsanta.dto.SorteoFormDTO;
import dev.jotxee.secretsanta.entity.PerfilSorteo;
import dev.jotxee.secretsanta.entity.Sorteo;
import dev.jotxee.secretsanta.entity.Usuario;
import dev.jotxee.secretsanta.event.SorteoCreatedEvent;
import dev.jotxee.secretsanta.repository.PerfilSorteoRepository;
import dev.jotxee.secretsanta.repository.SorteoRepository;
import dev.jotxee.secretsanta.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class SorteoService {

    private final SorteoRepository sorteoRepository;
    private final UsuarioRepository usuarioRepository;
    private final PerfilSorteoRepository perfilSorteoRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final PasswordEncoder passwordEncoder;
    private final PasswordGeneratorService passwordGeneratorService;
    private final EmailService emailService;
    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * Crea un sorteo completo con sus participantes y asignaciones de amigo invisible.
     * 
     * @param sorteoForm Formulario con los datos del sorteo y participantes
     * @return El sorteo creado
     * @throws IllegalArgumentException si hay menos de 3 participantes
     */
    @Transactional
    public Sorteo crearSorteo(SorteoFormDTO sorteoForm) {
        log.info("Iniciando creación de sorteo: {} con {} participantes", 
                sorteoForm.getNombre(), sorteoForm.getParticipantes().size());

        // Validaciones
        validarSorteo(sorteoForm);

        // Crear o recuperar usuarios, y crear perfiles de sorteo
        Map<String, String> passwordsPorEmail = new HashMap<>();
        List<PerfilSorteo> perfiles = crearPerfilesSorteoDesdeFormulario(sorteoForm, passwordsPorEmail);

        // Asignar amigos invisibles
        asignarAmigosInvisibles(perfiles);
        log.info("Asignaciones calculadas correctamente para {} perfiles", perfiles.size());

        // Guardar en base de datos
        Sorteo sorteo = guardarSorteoConPerfiles(
            sorteoForm.getNombre(), 
            sorteoForm.getNombreInterno(),
            sorteoForm.getImporteMinimo(),
            sorteoForm.getImporteMaximo(),
            perfiles
        );

        // Enviar emails con contraseñas (solo a nuevos usuarios)
        enviarPasswordsANuevosUsuarios(perfiles, passwordsPorEmail);

        // Publicar evento
        publicarEventoSorteoCreado(sorteo, perfiles);

        log.info("Sorteo creado exitosamente con ID: {}", sorteo.getId());
        return sorteo;
    }

    /**
     * Valida que el sorteo tenga los datos mínimos requeridos.
     */
    private void validarSorteo(SorteoFormDTO sorteoForm) {
        if (sorteoForm.getParticipantes() == null || sorteoForm.getParticipantes().isEmpty()) {
            throw new IllegalArgumentException("Debe añadir participantes al sorteo");
        }

        if (sorteoForm.getParticipantes().size() < 3) {
            throw new IllegalArgumentException("Se necesitan al menos 3 participantes para crear un sorteo");
        }
    }

    /**
     * Crea los perfiles de sorteo desde el formulario.
     * Si el usuario ya existe (por email), se reutiliza. Si no, se crea nuevo con contraseña.
     * Si el usuario existe pero tiene el password placeholder de migración, se regenera y envía.
     */
    private List<PerfilSorteo> crearPerfilesSorteoDesdeFormulario(SorteoFormDTO sorteoForm, Map<String, String> passwordsPorEmail) {
        List<PerfilSorteo> perfiles = new ArrayList<>();
        final String MIGRATION_PLACEHOLDER = "$2b$12$invalidinvalidinvalidinvalidinv";
        
        sorteoForm.getParticipantes().forEach(dto -> {
            String email = dto.getEmail().trim().toLowerCase();
            
            // Buscar o crear usuario
            Usuario usuario = usuarioRepository.findByEmail(email)
                .map(existingUser -> {
                    // Usuario existe - verificar si tiene password placeholder de migración
                    if (MIGRATION_PLACEHOLDER.equals(existingUser.getPassword())) {
                        // Regenerar contraseña para usuarios migrados con placeholder
                        String plainPassword = passwordGeneratorService.generatePassword();
                        passwordsPorEmail.put(email, plainPassword);
                        
                        existingUser.setPassword(passwordEncoder.encode(plainPassword));
                        Usuario updated = usuarioRepository.save(existingUser);
                        log.info("Password regenerado para usuario migrado: {}", updated.getNombre());
                        return updated;
                    }
                    log.debug("Usuario existente reutilizado: {}", existingUser.getNombre());
                    return existingUser;
                })
                .orElseGet(() -> {
                    // Usuario nuevo - crear con contraseña
                    String plainPassword = passwordGeneratorService.generatePassword();
                    passwordsPorEmail.put(email, plainPassword); // Guardar para enviar por email después
                    
                    Usuario nuevoUsuario = new Usuario();
                    nuevoUsuario.setEmail(email);
                    nuevoUsuario.setNombre(dto.getNombre().trim());
                    nuevoUsuario.setGenero(dto.getGenero());
                    nuevoUsuario.setPassword(passwordEncoder.encode(plainPassword));
                    nuevoUsuario.setRole("USER");
                    nuevoUsuario.setFechaCreacion(LocalDateTime.now());
                    
                    Usuario saved = usuarioRepository.save(nuevoUsuario);
                    log.info("Nuevo usuario creado: {}", saved.getNombre());
                    return saved;
                });

            // Crear perfil de sorteo
            PerfilSorteo perfil = new PerfilSorteo();
            perfil.setUsuario(usuario);
            perfil.setToken(UUID.randomUUID().toString());
            // sorteo se asignará al guardar
            
            perfiles.add(perfil);
        });

        return perfiles;
    }

    /**
     * Algoritmo para asignar amigos invisibles de forma aleatoria.
     * Garantiza que nadie se tenga a sí mismo.
     */
    private void asignarAmigosInvisibles(List<PerfilSorteo> perfiles) {
        int size = perfiles.size();
        
        // Crear lista de índices para barajar
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            indices.add(i);
        }

        boolean asignacionValida;
        int intentos = 0;
        final int MAX_INTENTOS = 100;

        do {
            Collections.shuffle(indices, secureRandom);
            asignacionValida = true;

            // Verificar que nadie se tenga a sí mismo
            for (int i = 0; i < size; i++) {
                if (indices.get(i) == i) {
                    asignacionValida = false;
                    break;
                }
            }

            intentos++;
            if (intentos >= MAX_INTENTOS) {
                log.error("No se pudo generar una asignación válida después de {} intentos", MAX_INTENTOS);
                throw new RuntimeException("Error al generar asignaciones de amigo invisible");
            }
        } while (!asignacionValida);

        // Asignar los emails según el orden barajado
        for (int i = 0; i < size; i++) {
            int asignadoIndex = indices.get(i);
            perfiles.get(i).setAsignadoA(perfiles.get(asignadoIndex).getUsuario().getEmail());
        }

        log.debug("Asignaciones generadas en {} intentos", intentos);
    }

    /**
     * Guarda el sorteo y sus perfiles en la base de datos.
     */
    private Sorteo guardarSorteoConPerfiles(String nombreSorteo, String nombreInterno, 
                                             Double importeMinimo, Double importeMaximo,
                                             List<PerfilSorteo> perfiles) {
        // Crear y guardar el sorteo
        Sorteo sorteo = new Sorteo();
        sorteo.setNombre(nombreSorteo);
        sorteo.setNombreInterno(nombreInterno);
        sorteo.setImporteMinimo(importeMinimo);
        sorteo.setImporteMaximo(importeMaximo);
        sorteo.setFechaCreacion(LocalDateTime.now());
        sorteo.setActivo(true);
        sorteo = sorteoRepository.save(sorteo);

        // Asignar el sorteo a todos los perfiles
        for (PerfilSorteo perfil : perfiles) {
            perfil.setSorteo(sorteo);
        }

        // Guardar todos los perfiles
        perfilSorteoRepository.saveAll(perfiles);
        
        log.debug("Sorteo y {} perfiles guardados en base de datos", perfiles.size());
        return sorteo;
    }

    /**
     * Envía las contraseñas por email solo a los usuarios nuevos o migrados con placeholder.
     * Los usuarios existentes con contraseñas válidas no reciben email.
     */
    private void enviarPasswordsANuevosUsuarios(List<PerfilSorteo> perfiles, Map<String, String> passwordsPorEmail) {
        perfiles.forEach(perfil -> {
            String email = perfil.getUsuario().getEmail();
            if (passwordsPorEmail.containsKey(email)) {
                String plainPassword = passwordsPorEmail.get(email);
                emailService.enviarPassword(email, perfil.getUsuario().getNombre(), plainPassword);
                log.info("Contraseña enviada a usuario: {}", perfil.getUsuario().getNombre());
            } else {
                log.debug("Usuario existente con contraseña válida, no se envía email: {}", perfil.getUsuario().getNombre());
            }
        });
    }

    /**
     * Publica el evento de sorteo creado para que los listeners lo procesen.
     * Este evento se publica dentro de la transacción para que los listeners
     * transaccionales se ejecuten después del commit.
     */
    private void publicarEventoSorteoCreado(Sorteo sorteo, List<PerfilSorteo> perfiles) {
        log.info("🚀 Publicando evento SorteoCreatedEvent para sorteo ID: {}", sorteo.getId());
        
        SorteoCreatedEvent evento = new SorteoCreatedEvent(
            sorteo.getId(),
            sorteo.getNombre(),
            sorteo.getImporteMinimo(),
            sorteo.getImporteMaximo(),
            perfiles.stream()
                    .map(p -> new SorteoCreatedEvent.ParticipantPayload(
                            p.getId(),
                            p.getUsuario().getNombre(),
                            p.getUsuario().getEmail(),
                            p.getAsignadoA(),
                            p.getToken()
                    ))
                    .toList()
        );
        
        eventPublisher.publishEvent(evento);
        log.debug("✅ Evento publicado correctamente");
    }
}
