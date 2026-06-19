package dev.jotxee.secretsanta.controller;

import dev.jotxee.secretsanta.dto.SorteoFormDTO;
import dev.jotxee.secretsanta.entity.Sorteo;
import dev.jotxee.secretsanta.repository.PerfilSorteoRepository;
import dev.jotxee.secretsanta.repository.SorteoRepository;
import dev.jotxee.secretsanta.service.SorteoService;
import dev.jotxee.secretsanta.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
public class CreateController {

    private final SorteoRepository sorteoRepository;
    private final SorteoService sorteoService;
    private final PerfilSorteoRepository perfilSorteoRepository;
    private final UsuarioService usuarioService;

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
            log.info("Recibida solicitud para crear sorteo: {}", sorteoForm.getNombre());
            
            // Delegar toda la lógica al servicio
            Sorteo sorteo = sorteoService.crearSorteo(sorteoForm);

            redirectAttributes.addFlashAttribute("success", 
                "¡Sorteo creado exitosamente! Se han asignado los amigos invisibles a " + 
                sorteo.getPerfiles().size() + " participantes.");
            
            return "redirect:/create";

        } catch (IllegalArgumentException e) {
            log.warn("Error de validación al crear sorteo: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/create";
        } catch (Exception e) {
            log.error("Error inesperado al crear sorteo", e);
            redirectAttributes.addFlashAttribute("error", 
                "Error al crear el sorteo: " + e.getMessage());
            return "redirect:/create";
        }
    }

    /**
     * Elimina un sorteo y todos sus participantes
     */
    @DeleteMapping("/sorteo/{id}")
    public String deleteSorteo(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            var sorteoOpt = sorteoRepository.findById(id);
            
            if (sorteoOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", 
                    "No se encontró el sorteo a eliminar");
                return "redirect:/create";
            }
            
            Sorteo sorteo = sorteoOpt.get();
            String nombreSorteo = sorteo.getNombre();
            
            // El cascade=ALL y orphanRemoval=true se encargan de eliminar participantes
            sorteoRepository.delete(sorteo);
            
            log.info("Sorteo eliminado: {} (ID: {})", nombreSorteo, id);
            
            redirectAttributes.addFlashAttribute("success", 
                "Sorteo \"" + nombreSorteo + "\" eliminado correctamente");
            
        } catch (Exception e) {
            log.error("Error al eliminar sorteo con ID: {}", id, e);
            redirectAttributes.addFlashAttribute("error", 
                "Error al eliminar el sorteo: " + e.getMessage());
        }
        
        return "redirect:/create";
    }

    /**
     * Regenera la contraseña de un USUARIO (no participante) y la envía por email.
     * El ID que llega es el de PerfilSorteo.
     */
    @PostMapping("/participante/{id}/regenerar-password")
    public String regenerarPassword(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            // El ID es de PerfilSorteo, necesitamos obtener el Usuario con FETCH JOIN
            var perfil = perfilSorteoRepository.findByIdWithUsuario(id)
                .orElseThrow(() -> new RuntimeException("Perfil no encontrado"));
            
            String email = perfil.getUsuario().getEmail();
            usuarioService.regenerarYEnviarPassword(email);

            log.info("Contraseña regenerada para usuario: {}", perfil.getUsuario().getNombre());

            redirectAttributes.addFlashAttribute("success",
                "Contraseña regenerada y enviada por email correctamente");

        } catch (Exception e) {
            log.error("Error al regenerar contraseña para perfil ID: {}", id, e);
            redirectAttributes.addFlashAttribute("error",
                "Error al regenerar la contraseña: " + e.getMessage());
        }

        return "redirect:/create";
    }
}
