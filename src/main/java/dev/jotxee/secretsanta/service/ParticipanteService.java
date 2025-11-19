package dev.jotxee.secretsanta.service;

import dev.jotxee.secretsanta.entity.Participante;
import dev.jotxee.secretsanta.entity.Sorteo;
import dev.jotxee.secretsanta.repository.ParticipanteRepository;
import dev.jotxee.secretsanta.repository.SorteoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ParticipanteService {

    public static final String PARTICIPANTE_NO_ENCONTRADO = "Participante no encontrado";
    private final ParticipanteRepository participanteRepository;
    private final SorteoRepository sorteoRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordGeneratorService passwordGeneratorService;
    private final EmailService emailService;

    @Transactional
    public String regenerarYEnviarPassword(Long participanteId) {
        Participante participante = participanteRepository.findById(participanteId)
            .orElseThrow(() -> new RuntimeException(PARTICIPANTE_NO_ENCONTRADO));

        String plainPassword = passwordGeneratorService.generatePassword();
        participante.setPassword(passwordEncoder.encode(plainPassword));
        participanteRepository.save(participante);

        emailService.enviarPassword(participante.getEmail(), participante.getNombre(), plainPassword);
        log.info("Contraseña regenerada y enviada para participante: {}", participante.getId());

        return plainPassword;
    }

    @Transactional
    public void actualizarTallas(Long participanteId, String tallaCamisa, String tallaPantalon,
                                  String tallaZapato, String tallaChaqueta, String preferencias) {
        Participante participante = participanteRepository.findById(participanteId)
            .orElseThrow(() -> new RuntimeException(PARTICIPANTE_NO_ENCONTRADO));

        participante.setTallaCamisa(tallaCamisa);
        participante.setTallaPantalon(tallaPantalon);
        participante.setTallaZapato(tallaZapato);
        participante.setTallaChaqueta(tallaChaqueta);
        participante.setPreferencias(preferencias);

        participanteRepository.save(participante);
        log.info("Tallas actualizadas para participante: {}", participante.getId());
    }

    /**
     * Obtiene todos los sorteos en los que participa un usuario
     */
    @Transactional(readOnly = true)
    public List<Sorteo> obtenerSorteosDelParticipante(Long participanteId) {
        Participante participante = participanteRepository.findByIdWithSorteo(participanteId)
            .orElseThrow(() -> new RuntimeException(PARTICIPANTE_NO_ENCONTRADO));

        // Buscar todos los participantes con el mismo email (con su sorteo cargado)
        List<Participante> todasLasParticipaciones = participanteRepository.findByEmailWithSorteo(participante.getEmail());

        // Obtener los sorteos únicos por ID para evitar problemas con proxies
        return todasLasParticipaciones.stream()
            .map(Participante::getSorteo)
            .filter(sorteo -> sorteo != null)
            .collect(Collectors.toMap(
                Sorteo::getId,
                sorteo -> sorteo,
                (existing, replacement) -> existing
            ))
            .values()
            .stream()
            .collect(Collectors.toList());
    }

    /**
     * Obtiene los participantes asignados a un usuario en un sorteo específico
     */
    public List<Participante> obtenerAsignadosEnSorteo(Long participanteId, Long sorteoId) {
        Participante participante = participanteRepository.findById(participanteId)
            .orElseThrow(() -> new RuntimeException(PARTICIPANTE_NO_ENCONTRADO));

        // Supongo que se esta validando que existe el sorteo
        sorteoRepository.findById(sorteoId)
            .orElseThrow(() -> new RuntimeException("Sorteo no encontrado"));

        // Verificar que el participante está en ese sorteo
        boolean estaEnSorteo = participanteRepository.findBySorteoId(sorteoId).stream()
            .anyMatch(p -> p.getEmail().equals(participante.getEmail()));

        if (!estaEnSorteo) {
            throw new SecurityException("No tienes acceso a este sorteo");
        }

        // Buscar quién tiene asignado a este participante en ese sorteo
        List<Participante> asignados = new ArrayList<>();
        List<Participante> todosEnSorteo = participanteRepository.findBySorteoId(sorteoId);

        for (Participante p : todosEnSorteo) {
            if (p.getAsignadoA() != null && p.getAsignadoA().equals(participante.getEmail())) {
                asignados.add(p);
            }
        }

        return asignados;
    }

    public Participante obtenerPorId(Long id) {
        return participanteRepository.findByIdWithSorteo(id)
            .orElseThrow(() -> new RuntimeException(PARTICIPANTE_NO_ENCONTRADO));
    }
}

