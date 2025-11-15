package dev.jotxee.secretsanta.controller;

import dev.jotxee.secretsanta.dto.ParticipanteFormDTO;
import dev.jotxee.secretsanta.dto.SorteoFormDTO;
import dev.jotxee.secretsanta.entity.Participante;
import dev.jotxee.secretsanta.entity.Sorteo;
import dev.jotxee.secretsanta.repository.ParticipanteRepository;
import dev.jotxee.secretsanta.repository.SorteoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.*;

@Controller
@RequiredArgsConstructor
@Slf4j
public class CreateController {

    private final SorteoRepository sorteoRepository;
    private final ParticipanteRepository participanteRepository;

    @GetMapping("/create")
    public String showCreatePage(Model model) {
        List<Sorteo> sorteos = sorteoRepository.findByActivoTrue();
        model.addAttribute("sorteos", sorteos);
        
        // Los mensajes flash se añaden automáticamente al modelo por Spring
        // pero necesitamos asegurarnos de que existan como atributos
        if (!model.containsAttribute("success")) {
            model.addAttribute("success", null);
        }
        if (!model.containsAttribute("error")) {
            model.addAttribute("error", null);
        }
        
        return "create";
    }

    @PostMapping("/create")
    public String createSorteo(
            @ModelAttribute SorteoFormDTO sorteoForm,
            RedirectAttributes redirectAttributes) {
        
        try {
            // Validar que hay participantes
            if (sorteoForm.getParticipantes() == null || sorteoForm.getParticipantes().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", 
                    "Debe añadir participantes al sorteo");
                return "redirect:/create";
            }

            // Validar que hay al menos 3 participantes
            if (sorteoForm.getParticipantes().size() < 3) {
                redirectAttributes.addFlashAttribute("error", 
                    "Se necesitan al menos 3 participantes para crear un sorteo");
                return "redirect:/create";
            }

            // Crear entidades en memoria (sin tocar BD todavía)
            log.info("Creando sorteo: {} con {} participantes", 
                sorteoForm.getNombre(), sorteoForm.getParticipantes().size());

            // Crear lista de participantes temporales (sin sorteo asignado aún)
            List<Participante> participantes = new ArrayList<>();
            for (ParticipanteFormDTO dto : sorteoForm.getParticipantes()) {
                Participante participante = new Participante();
                participante.setNombre(dto.getNombre().trim());
                participante.setEmail(dto.getEmail().trim());
                participante.setToken(UUID.randomUUID().toString());
                participantes.add(participante);
            }

            // ⚡ Calcular asignaciones SIN conexión a BD (puro cálculo en memoria)
            asignarAmigosInvisibles(participantes);
            log.info("Asignaciones calculadas correctamente");

            // 💾 Ahora SÍ guardamos todo en una transacción atómica
            guardarSorteoCompleto(sorteoForm.getNombre(), participantes);

            log.info("Sorteo y participantes guardados exitosamente");

            redirectAttributes.addFlashAttribute("success", 
                "¡Sorteo creado exitosamente! Se han asignado los amigos invisibles a " + 
                participantes.size() + " participantes.");

            // TODO: Enviar emails aquí cuando implementemos EmailService
            
            return "redirect:/create";

        } catch (Exception e) {
            log.error("Error al crear sorteo", e);
            redirectAttributes.addFlashAttribute("error", 
                "Error al crear el sorteo: " + e.getMessage());
            return "redirect:/create";
        }
    }

    /**
     * Guarda el sorteo y sus participantes en una transacción atómica.
     * Si algo falla, se hace rollback de todo.
     */
    @Transactional
    protected void guardarSorteoCompleto(String nombreSorteo, List<Participante> participantes) {
        // Crear y guardar el sorteo
        Sorteo sorteo = new Sorteo();
        sorteo.setNombre(nombreSorteo);
        sorteo.setFechaCreacion(LocalDateTime.now());
        sorteo.setActivo(true);
        sorteo = sorteoRepository.save(sorteo);

        // Asignar el sorteo a todos los participantes
        for (Participante participante : participantes) {
            participante.setSorteo(sorteo);
        }

        // Guardar todos los participantes de una vez
        participanteRepository.saveAll(participantes);
    }    /**
     * Algoritmo para asignar amigos invisibles de forma aleatoria
     * Garantiza que nadie se tenga a sí mismo
     */
    private void asignarAmigosInvisibles(List<Participante> participantes) {
        int size = participantes.size();
        
        if (size < 3) {
            throw new IllegalArgumentException("Se necesitan al menos 3 participantes");
        }

        // Crear lista de índices y mezclarla
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            indices.add(i);
        }

        // Intentar hasta conseguir una asignación válida (sin auto-asignaciones)
        boolean valid = false;
        int intentos = 0;
        final int MAX_INTENTOS = 100;

        while (!valid && intentos < MAX_INTENTOS) {
            Collections.shuffle(indices);
            valid = true;

            // Verificar que nadie se tiene a sí mismo
            for (int i = 0; i < size; i++) {
                if (indices.get(i) == i) {
                    valid = false;
                    break;
                }
            }
            intentos++;
        }

        if (!valid) {
            throw new RuntimeException("No se pudo generar una asignación válida después de " + MAX_INTENTOS + " intentos");
        }

        // Asignar los amigos invisibles
        for (int i = 0; i < size; i++) {
            int asignadoIndex = indices.get(i);
            participantes.get(i).setAsignadoA(participantes.get(asignadoIndex).getNombre());
            
            log.debug("Asignación: {} → {}", 
                participantes.get(i).getNombre(), 
                participantes.get(asignadoIndex).getNombre());
        }
    }
}
