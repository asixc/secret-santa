package dev.jotxee.secretsanta.controller;

import dev.jotxee.secretsanta.entity.PerfilSorteo;
import dev.jotxee.secretsanta.entity.Sorteo;
import dev.jotxee.secretsanta.entity.Usuario;
import dev.jotxee.secretsanta.security.ParticipanteUserDetails;
import dev.jotxee.secretsanta.service.UsuarioService;
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

    private final UsuarioService usuarioService;

    @GetMapping
    public String showMyProfile(@AuthenticationPrincipal ParticipanteUserDetails userDetails, Model model) {
        // Obtener usuario y sus perfiles
        Usuario usuario = usuarioService.obtenerPorEmail(userDetails.getUsername());
        List<PerfilSorteo> perfiles = usuarioService.obtenerPerfilesDelUsuario(usuario.getId());
        List<Sorteo> sorteos = usuarioService.obtenerSorteosDelUsuario(usuario.getId());

        model.addAttribute("usuario", usuario);
        model.addAttribute("perfiles", perfiles);
        model.addAttribute("sorteos", sorteos);
        model.addAttribute("viendoUsuario", usuario); // Por defecto vemos nuestro perfil

        return "my-profile";
    }

    @GetMapping("/usuario/{id}")
    public String verUsuario(
            @PathVariable Long id,
            @AuthenticationPrincipal ParticipanteUserDetails userDetails,
            Model model,
            RedirectAttributes redirectAttributes) {

        try {
            Usuario miUsuario = usuarioService.obtenerPorEmail(userDetails.getUsername());
            Usuario usuarioAVer = usuarioService.obtenerPorId(id);

            // Verificar que ambos comparten al menos un sorteo
            List<Sorteo> misSorteos = usuarioService.obtenerSorteosDelUsuario(miUsuario.getId());
            List<Sorteo> sussSorteos = usuarioService.obtenerSorteosDelUsuario(usuarioAVer.getId());

            // Obtener los IDs de los sorteos compartidos
            List<Long> sorteosCompartidosIds = misSorteos.stream()
                .filter(miSorteo -> sussSorteos.stream()
                    .anyMatch(suSorteo -> suSorteo.getId().equals(miSorteo.getId())))
                .map(Sorteo::getId)
                .toList();

            if (sorteosCompartidosIds.isEmpty()) {
                throw new SecurityException("No tienes permiso para ver este perfil");
            }

            List<PerfilSorteo> misPerfiles = usuarioService.obtenerPerfilesDelUsuario(miUsuario.getId());
            List<PerfilSorteo> todosLosPerfilesDelUsuarioAVer = usuarioService.obtenerPerfilesDelUsuario(usuarioAVer.getId());
            
            // FILTRAR: Solo mostrar los perfiles de los sorteos compartidos
            List<PerfilSorteo> perfilesCompartidos = todosLosPerfilesDelUsuarioAVer.stream()
                .filter(perfil -> sorteosCompartidosIds.contains(perfil.getSorteo().getId()))
                .toList();

            model.addAttribute("usuario", miUsuario);
            model.addAttribute("perfiles", misPerfiles);
            model.addAttribute("sorteos", misSorteos);
            model.addAttribute("viendoUsuario", usuarioAVer);
            model.addAttribute("viendoPerfiles", perfilesCompartidos);

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
            @RequestParam Long perfilId,
            @RequestParam(required = false) String tallaCamisa,
            @RequestParam(required = false) String tallaPantalon,
            @RequestParam(required = false) String tallaZapato,
            @RequestParam(required = false) String tallaChaqueta,
            @RequestParam(required = false) String preferencias,
            RedirectAttributes redirectAttributes) {

        try {
            Usuario usuario = usuarioService.obtenerPorEmail(userDetails.getUsername());
            PerfilSorteo perfil = usuarioService.obtenerPerfilPorId(perfilId);

            // Verificar que el perfil pertenece al usuario autenticado
            if (!perfil.getUsuario().getId().equals(usuario.getId())) {
                throw new SecurityException("No puedes modificar este perfil");
            }

            usuarioService.actualizarTallas(
                perfilId,
                tallaCamisa,
                tallaPantalon,
                tallaZapato,
                tallaChaqueta,
                preferencias
            );

            redirectAttributes.addFlashAttribute("success", "Tallas actualizadas correctamente");
            log.info("Tallas actualizadas para perfil: {} del usuario: {}", perfilId, usuario.getEmail());

        } catch (SecurityException e) {
            log.warn("Intento de modificación no autorizado: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", "No tienes permiso para modificar este perfil");
        } catch (Exception e) {
            log.error("Error al actualizar tallas", e);
            redirectAttributes.addFlashAttribute("error", "Error al actualizar las tallas");
        }

        return "redirect:/my-profile";
    }

    @GetMapping("/sorteo/{sorteoId}/perfiles")
    @ResponseBody
    public List<PerfilSorteo> obtenerPerfilesSorteo(
            @PathVariable Long sorteoId,
            @AuthenticationPrincipal ParticipanteUserDetails userDetails) {

        Usuario usuario = usuarioService.obtenerPorEmail(userDetails.getUsername());
        
        // Verificar que el usuario participa en este sorteo
        List<Sorteo> misSorteos = usuarioService.obtenerSorteosDelUsuario(usuario.getId());
        boolean participaEnSorteo = misSorteos.stream().anyMatch(s -> s.getId().equals(sorteoId));
        
        if (!participaEnSorteo) {
            throw new SecurityException("No tienes permiso para ver este sorteo");
        }

        return usuarioService.obtenerPerfilesPorSorteo(sorteoId);
    }
}

