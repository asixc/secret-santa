package dev.jotxee.secretsanta.controller;

import dev.jotxee.secretsanta.service.SecretSantaService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class IndexController {

    private final SecretSantaService secretSantaService;

    public IndexController(SecretSantaService secretSantaService) {
        this.secretSantaService = secretSantaService;
    }

    @GetMapping("/")
    public String index(@RequestParam(required = false) String id, Model model) {
        if (id != null && !id.isBlank()) {
            try {
                var revealData = secretSantaService.getRevealData(id);
                model.addAttribute("hasToken", true);
                model.addAttribute("names", revealData.names());
                model.addAttribute("assignedName", revealData.assigned());
            } catch (Exception e) {
                // Token inválido o no encontrado, mostrar mensaje de bienvenida
                model.addAttribute("hasToken", false);
            }
        } else {
            model.addAttribute("hasToken", false);
        }
        return "index";
    }
}
