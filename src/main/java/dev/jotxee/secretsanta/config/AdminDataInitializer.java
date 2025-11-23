package dev.jotxee.secretsanta.config;

import dev.jotxee.secretsanta.entity.Usuario;
import dev.jotxee.secretsanta.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AdminDataInitializer {

    private static final Logger logger = LoggerFactory.getLogger(AdminDataInitializer.class);

    @Value("${admin.user}")
    private String adminUsername;

    @Value("${admin.password}")
    private String adminPassword;

    @Bean
    CommandLineRunner initAdminUser(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder) {

        return args -> {
            logger.info("🔍 Verificando usuario administrador...");

            if (usuarioRepository.findByEmail(adminUsername).isEmpty()) {
                logger.info("👤 Creando usuario administrador: {}", adminUsername);

                // Hashear la contraseña antes de guardarla
                String hashedPassword = passwordEncoder.encode(adminPassword);
                logger.debug("🔐 Contraseña hasheada con BCrypt");

                Usuario admin = new Usuario();
                admin.setEmail(adminUsername);
                admin.setPassword(hashedPassword);
                admin.setNombre("Administrador");
                admin.setRole("ADMIN");

                usuarioRepository.save(admin);
                logger.info("✅ Usuario administrador creado exitosamente");
            } else {
                logger.info("✓ Usuario administrador ya existe");
            }
        };
    }
}

