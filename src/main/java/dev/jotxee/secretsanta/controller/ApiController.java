package dev.jotxee.secretsanta.controller;

import dev.jotxee.secretsanta.dto.RevealResponse;
import dev.jotxee.secretsanta.service.SecretSantaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ApiController {
    
    private final SecretSantaService secretSantaService;
    
    @GetMapping("/reveal")
    public ResponseEntity<RevealResponse> reveal(@RequestParam String token) {
        try {
            RevealResponse response = secretSantaService.getRevealData(token);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
