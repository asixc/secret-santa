package dev.jotxee.secretsanta.controller;

import dev.jotxee.secretsanta.entity.Participante;
import dev.jotxee.secretsanta.entity.Sorteo;
import dev.jotxee.secretsanta.security.ParticipanteUserDetails;
import dev.jotxee.secretsanta.service.ParticipanteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/my-profile")
@RequiredArgsConstructor
@Slf4j
public class MyProfileController {

    private final ParticipanteService participanteService;

    @GetMapping
    public String showMyProfile(@AuthenticationPrincipal ParticipanteUserDetails userDetails, Model model) {
        Participante participante = userDetails.getParticipante();
        List<Sorteo> sorteos = participanteService.obtenerSorteosDelParticipante(participante.getId());

        model.addAttribute("participante", participante);
        model.addAttribute("sorteos", sorteos);
        model.addAttribute("viendoParticipante", participante); // Por defecto vemos nuestro perfil

        return "my-profile";
    }

    @GetMapping("/participante/{id}")
    public String verParticipante(
            @PathVariable Long id,
            @AuthenticationPrincipal ParticipanteUserDetails userDetails,
            Model model,
            RedirectAttributes redirectAttributes) {

        try {
            Participante miParticipante = userDetails.getParticipante();
            Participante participanteAVer = participanteService.obtenerPorId(id);

            // Verificar que ambos están en el mismo sorteo
            boolean compartenSorteo = participanteService.obtenerSorteosDelParticipante(miParticipante.getId())
                .stream()
                .anyMatch(sorteo -> participanteAVer.getSorteo().getId().equals(sorteo.getId()));

            if (!compartenSorteo) {
                throw new SecurityException("No tienes permiso para ver este perfil");
            }

            List<Sorteo> misSorteos = participanteService.obtenerSorteosDelParticipante(miParticipante.getId());

            model.addAttribute("participante", miParticipante);
            model.addAttribute("sorteos", misSorteos);
            model.addAttribute("viendoParticipante", participanteAVer);

            return "my-profile";

        } catch (SecurityException e) {
            log.warn("Intento de acceso no autorizado: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", "No tienes permiso para ver ese perfil");
            return "redirect:/my-profile";
        }
    }

    @PostMapping("/actualizar-tallas")
    public String actualizarTallas(
            @AuthenticationPrincipal ParticipanteUserDetails userDetails,
            @RequestParam(required = false) String tallaCamisa,
            @RequestParam(required = false) String tallaPantalon,
            @RequestParam(required = false) String tallaZapato,
            @RequestParam(required = false) String tallaChaqueta,
            @RequestParam(required = false) String preferencias,
            RedirectAttributes redirectAttributes) {

        try {
            Participante participante = userDetails.getParticipante();

            participanteService.actualizarTallas(
                participante.getId(),
                tallaCamisa,
                tallaPantalon,
                tallaZapato,
                tallaChaqueta,
                preferencias
            );

            redirectAttributes.addFlashAttribute("success", "Tallas actualizadas correctamente");
            log.info("Tallas actualizadas para participante: {}", participante.getId());

        } catch (Exception e) {
            log.error("Error al actualizar tallas", e);
            redirectAttributes.addFlashAttribute("error", "Error al actualizar las tallas");
        }

        return "redirect:/my-profile";
    }

    @GetMapping("/sorteo/{sorteoId}/asignados")
    @ResponseBody
    public List<Participante> obtenerAsignados(
            @PathVariable Long sorteoId,
            @AuthenticationPrincipal ParticipanteUserDetails userDetails) {

        Participante participante = userDetails.getParticipante();
        return participanteService.obtenerAsignadosEnSorteo(participante.getId(), sorteoId);
    }
}

