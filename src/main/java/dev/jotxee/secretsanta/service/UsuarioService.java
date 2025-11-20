package dev.jotxee.secretsanta.service;

import dev.jotxee.secretsanta.entity.PerfilSorteo;
import dev.jotxee.secretsanta.entity.Sorteo;
import dev.jotxee.secretsanta.entity.Usuario;
import dev.jotxee.secretsanta.repository.PerfilSorteoRepository;
import dev.jotxee.secretsanta.repository.SorteoRepository;
import dev.jotxee.secretsanta.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UsuarioService {

    public static final String USUARIO_NO_ENCONTRADO = "Usuario no encontrado";
    public static final String PERFIL_NO_ENCONTRADO = "Perfil de sorteo no encontrado";
    
    private final UsuarioRepository usuarioRepository;
    private final PerfilSorteoRepository perfilSorteoRepository;
    private final SorteoRepository sorteoRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordGeneratorService passwordGeneratorService;
    private final EmailService emailService;

    @Transactional
    public String regenerarYEnviarPassword(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException(USUARIO_NO_ENCONTRADO));

        String plainPassword = passwordGeneratorService.generatePassword();
        usuario.setPassword(passwordEncoder.encode(plainPassword));
        usuarioRepository.save(usuario);

        emailService.enviarPassword(usuario.getEmail(), usuario.getNombre(), plainPassword);
        log.info("Contraseña regenerada y enviada para usuario: {}", usuario.getEmail());

        return plainPassword;
    }

    @Transactional
    public void actualizarTallas(Long perfilId, String tallaCamisa, String tallaPantalon,
                                  String tallaZapato, String tallaChaqueta, String preferencias) {
        PerfilSorteo perfil = perfilSorteoRepository.findById(perfilId)
            .orElseThrow(() -> new RuntimeException(PERFIL_NO_ENCONTRADO));

        perfil.setTallaCamisa(tallaCamisa);
        perfil.setTallaPantalon(tallaPantalon);
        perfil.setTallaZapato(tallaZapato);
        perfil.setTallaChaqueta(tallaChaqueta);
        perfil.setPreferencias(preferencias);

        perfilSorteoRepository.save(perfil);
        log.info("Tallas actualizadas para perfil: {} del usuario: {}", perfil.getId(), perfil.getUsuario().getEmail());
    }

    /**
     * Obtiene un usuario por email
     */
    @Transactional(readOnly = true)
    public Usuario obtenerPorEmail(String email) {
        return usuarioRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException(USUARIO_NO_ENCONTRADO));
    }

    /**
     * Obtiene un usuario por ID
     */
    @Transactional(readOnly = true)
    public Usuario obtenerPorId(Long usuarioId) {
        return usuarioRepository.findById(usuarioId)
            .orElseThrow(() -> new RuntimeException(USUARIO_NO_ENCONTRADO));
    }

    /**
     * Obtiene un perfil de sorteo por ID
     */
    @Transactional(readOnly = true)
    public PerfilSorteo obtenerPerfilPorId(Long perfilId) {
        return perfilSorteoRepository.findById(perfilId)
            .orElseThrow(() -> new RuntimeException(PERFIL_NO_ENCONTRADO));
    }

    /**
     * Obtiene todos los perfiles de sorteo de un usuario
     */
    @Transactional(readOnly = true)
    public List<PerfilSorteo> obtenerPerfilesDelUsuario(Long usuarioId) {
        return perfilSorteoRepository.findByUsuarioIdWithDetails(usuarioId);
    }

    /**
     * Obtiene todos los perfiles de sorteo de un usuario por email
     */
    @Transactional(readOnly = true)
    public List<PerfilSorteo> obtenerPerfilesDelUsuarioPorEmail(String email) {
        Usuario usuario = obtenerPorEmail(email);
        return perfilSorteoRepository.findByUsuarioIdWithDetails(usuario.getId());
    }

    /**
     * Obtiene todos los sorteos en los que participa un usuario
     */
    @Transactional(readOnly = true)
    public List<Sorteo> obtenerSorteosDelUsuario(Long usuarioId) {
        List<PerfilSorteo> perfiles = perfilSorteoRepository.findByUsuarioIdWithDetails(usuarioId);
        
        // Obtener los IDs únicos de sorteos
        List<Long> sorteoIds = perfiles.stream()
            .map(PerfilSorteo::getSorteo)
            .filter(sorteo -> sorteo != null)
            .map(Sorteo::getId)
            .distinct()
            .collect(Collectors.toList());

        // Cargar sorteos con sus perfiles eagerly para evitar LazyInitializationException
        return sorteoIds.isEmpty() ? List.of() : sorteoRepository.findByIdInWithPerfiles(sorteoIds);
    }

    /**
     * Obtiene todos los perfiles de un sorteo específico
     */
    @Transactional(readOnly = true)
    public List<PerfilSorteo> obtenerPerfilesPorSorteo(Long sorteoId) {
        return perfilSorteoRepository.findBySorteoIdWithUsuario(sorteoId);
    }
}
