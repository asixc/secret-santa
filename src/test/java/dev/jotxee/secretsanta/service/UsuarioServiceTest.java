package dev.jotxee.secretsanta.service;

import dev.jotxee.secretsanta.entity.PerfilSorteo;
import dev.jotxee.secretsanta.entity.Sorteo;
import dev.jotxee.secretsanta.entity.Usuario;
import dev.jotxee.secretsanta.repository.PerfilSorteoRepository;
import dev.jotxee.secretsanta.repository.SorteoRepository;
import dev.jotxee.secretsanta.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PerfilSorteoRepository perfilSorteoRepository;

    @Mock
    private SorteoRepository sorteoRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private PasswordGeneratorService passwordGeneratorService;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private UsuarioService usuarioService;

    @Test
    void actualizarPerfilSorteo_actualizaSoloElPerfilDelSorteoSolicitado() {
        PerfilSorteo perfilSorteoA = new PerfilSorteo();
        PerfilSorteo perfilSorteoB = new PerfilSorteo();
        Usuario usuario = new Usuario();
        usuario.setNombre("Usuario de prueba");
        perfilSorteoA.setUsuario(usuario);
        when(perfilSorteoRepository.findByIdAndUsuarioId(10L, 7L))
            .thenReturn(Optional.of(perfilSorteoA));

        PerfilSorteo actualizado = usuarioService.actualizarPerfilSorteo(
            7L, 10L, "M", "42", "43", "L", "Libros"
        );

        assertSame(perfilSorteoA, actualizado);
        assertEquals("M", perfilSorteoA.getTallaCamisa());
        assertEquals("42", perfilSorteoA.getTallaPantalon());
        assertEquals("43", perfilSorteoA.getTallaZapato());
        assertEquals("L", perfilSorteoA.getTallaChaqueta());
        assertEquals("Libros", perfilSorteoA.getPreferencias());
        assertNull(perfilSorteoB.getTallaCamisa());
        verify(perfilSorteoRepository).save(perfilSorteoA);
    }

    @Test
    void actualizarPerfilSorteo_rechazaUnPerfilDeOtroUsuario() {
        when(perfilSorteoRepository.findByIdAndUsuarioId(10L, 7L))
            .thenReturn(Optional.empty());

        assertThrows(SecurityException.class, () -> usuarioService.actualizarPerfilSorteo(
            7L, 10L, "M", "42", "43", "L", "Libros"
        ));

        verify(perfilSorteoRepository, never()).save(any(PerfilSorteo.class));
    }

    @Test
    void regenerarYEnviarPassword_guardaLaNuevaPasswordYLaEnvia() {
        Usuario usuario = usuario(7L, "usuario@example.com", "Nombre");
        when(usuarioRepository.findByEmail(usuario.getEmail())).thenReturn(Optional.of(usuario));
        when(passwordGeneratorService.generatePassword()).thenReturn("nueva-password");
        when(passwordEncoder.encode("nueva-password")).thenReturn("password-cifrada");

        String password = usuarioService.regenerarYEnviarPassword(usuario.getEmail());

        assertEquals("nueva-password", password);
        assertEquals("password-cifrada", usuario.getPassword());
        verify(usuarioRepository).save(usuario);
        verify(emailService).enviarPassword(usuario.getEmail(), usuario.getNombre(), "nueva-password");
    }

    @Test
    void obtenerPerfilesDelUsuario_ordenaPorSorteoMasReciente() {
        PerfilSorteo perfilAntiguo = perfil(1L, sorteo(1L, LocalDateTime.of(2025, 1, 1, 0, 0)));
        PerfilSorteo perfilReciente = perfil(2L, sorteo(2L, LocalDateTime.of(2026, 1, 1, 0, 0)));
        when(perfilSorteoRepository.findByUsuarioId(7L))
            .thenReturn(new ArrayList<>(List.of(perfilAntiguo, perfilReciente)));

        List<PerfilSorteo> perfiles = usuarioService.obtenerPerfilesDelUsuario(7L);

        assertIterableEquals(List.of(perfilReciente, perfilAntiguo), perfiles);
    }

    @Test
    void obtenerSorteosDelUsuario_eliminaDuplicadosYOrdenaPorFecha() {
        Sorteo sorteoAntiguo = sorteo(1L, LocalDateTime.of(2025, 1, 1, 0, 0));
        Sorteo sorteoReciente = sorteo(2L, LocalDateTime.of(2026, 1, 1, 0, 0));
        when(perfilSorteoRepository.findByUsuarioId(7L)).thenReturn(List.of(
            perfil(10L, sorteoAntiguo),
            perfil(11L, sorteoAntiguo),
            perfil(12L, sorteoReciente)
        ));
        when(sorteoRepository.findByIdInWithPerfiles(List.of(1L, 2L)))
            .thenReturn(new ArrayList<>(List.of(sorteoAntiguo, sorteoReciente)));

        List<Sorteo> sorteos = usuarioService.obtenerSorteosDelUsuario(7L);

        assertIterableEquals(List.of(sorteoReciente, sorteoAntiguo), sorteos);
    }

    @Test
    void obtenerSorteosDelUsuario_sinPerfilesDevuelveListaVacia() {
        when(perfilSorteoRepository.findByUsuarioId(7L)).thenReturn(List.of());

        List<Sorteo> sorteos = usuarioService.obtenerSorteosDelUsuario(7L);

        assertEquals(List.of(), sorteos);
        verify(sorteoRepository, never()).findByIdInWithPerfiles(any());
    }

    @Test
    void obtenerSorteosCompartidos_ordenaPorFechaMasReciente() {
        Sorteo sorteoAntiguo = sorteo(1L, LocalDateTime.of(2025, 1, 1, 0, 0));
        Sorteo sorteoReciente = sorteo(2L, LocalDateTime.of(2026, 1, 1, 0, 0));
        when(sorteoRepository.findSharedSorteos(7L, 8L))
            .thenReturn(new ArrayList<>(List.of(sorteoAntiguo, sorteoReciente)));

        List<Sorteo> sorteos = usuarioService.obtenerSorteosCompartidos(7L, 8L);

        assertIterableEquals(List.of(sorteoReciente, sorteoAntiguo), sorteos);
    }

    private static Usuario usuario(Long id, String email, String nombre) {
        Usuario usuario = new Usuario();
        usuario.setId(id);
        usuario.setEmail(email);
        usuario.setNombre(nombre);
        return usuario;
    }

    private static Sorteo sorteo(Long id, LocalDateTime fechaCreacion) {
        Sorteo sorteo = new Sorteo();
        sorteo.setId(id);
        sorteo.setFechaCreacion(fechaCreacion);
        return sorteo;
    }

    private static PerfilSorteo perfil(Long id, Sorteo sorteo) {
        PerfilSorteo perfil = new PerfilSorteo();
        perfil.setId(id);
        perfil.setSorteo(sorteo);
        return perfil;
    }
}
