package dev.jotxee.secretsanta.security;

import dev.jotxee.secretsanta.entity.Usuario;
import dev.jotxee.secretsanta.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class LoginNormalizationIntegrationTest {

    private static final String EMAIL = "normalized-login@example.com";
    private static final String PASSWORD = "Abc123!@#xyz";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        usuarioRepository.findByEmail(EMAIL).ifPresent(usuarioRepository::delete);

        Usuario usuario = new Usuario();
        usuario.setEmail(EMAIL);
        usuario.setPassword(passwordEncoder.encode(PASSWORD));
        usuario.setNombre("Usuario Normalizado");
        usuario.setRole("USER");
        usuarioRepository.save(usuario);
    }

    @Test
    void logsInWithWhitespaceAroundLoginParameters() throws Exception {
        mockMvc.perform(post("/login")
                        .param("username", " " + EMAIL + " ")
                        .param("password", " " + PASSWORD + "\n"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/my-profile"));
    }

    @Test
    void logsInWithLineBreakInsideGeneratedPassword() throws Exception {
        mockMvc.perform(post("/login")
                        .param("username", EMAIL)
                        .param("password", "Abc123\n!@#xyz"))
                .andExpect(redirectedUrl("/my-profile"));
    }

    @Test
    void rejectsIncorrectPasswordAfterNormalization() throws Exception {
        mockMvc.perform(post("/login")
                        .param("username", EMAIL)
                        .param("password", " Abc123\n!@#xyZ "))
                .andExpect(redirectedUrl("/login?error"));
    }

    @Test
    void preservesSpacesInsidePassword() throws Exception {
        Usuario usuario = usuarioRepository.findByEmail(EMAIL).orElseThrow();
        usuario.setPassword(passwordEncoder.encode("frase secreta"));
        usuarioRepository.save(usuario);

        mockMvc.perform(post("/login")
                        .param("username", EMAIL)
                        .param("password", " frase secreta "))
                .andExpect(redirectedUrl("/my-profile"));

        mockMvc.perform(post("/login")
                        .param("username", EMAIL)
                        .param("password", "frasesecreta"))
                .andExpect(redirectedUrl("/login?error"));
    }

    @Test
    void doesNotRemoveSpacesInsideUsername() throws Exception {
        mockMvc.perform(post("/login")
                        .param("username", "normalized-login @example.com")
                        .param("password", PASSWORD))
                .andExpect(redirectedUrl("/login?error"));
    }
}
