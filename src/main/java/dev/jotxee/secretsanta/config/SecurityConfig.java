package dev.jotxee.secretsanta.config;

import dev.jotxee.secretsanta.security.ParticipanteUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.util.matcher.RequestMatcher;

import java.util.List;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final ParticipanteUserDetailsService participanteUserDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final CustomAuthEntryPoint customAuthEntryPoint;

    @Value("${admin.username:admin}")
    private String adminUsername;

    @Value("${admin.password:admin123}")
    private String adminPassword;

    @Bean
    public DaoAuthenticationProvider participanteAuthenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(participanteUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager() {
        // AuthenticationManager con un único provider que gestiona tanto admins como users desde BD
        return new ProviderManager(List.of(
            participanteAuthenticationProvider()
        ));
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Autorizaciones
            .authorizeHttpRequests(authz -> authz
                .requestMatchers(
                    "/", "/login", "/error",
                    "/css/**", "/js/**", "/images/**", "/audio/**", "/favicon.ico"
                ).permitAll()
                .requestMatchers("/create/**", "/api/**").hasRole("ADMIN")
                .requestMatchers("/my-profile/**").hasRole("USER")
                .anyRequest().permitAll()
            )
            // Login por formulario con redirección condicional
            .formLogin(form -> form
                .loginPage("/login")
                .permitAll()
                .successHandler((request, response, authentication) -> {
                    log.info("Login exitoso para: {}", authentication.getName());
                    if (authentication.getAuthorities().stream()
                            .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
                        response.sendRedirect("/create");
                    } else {
                        response.sendRedirect("/my-profile");
                    }
                })
                .failureHandler(authenticationFailureHandler())
            )
            // Logout sencillo
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .permitAll()
            )
            // API devuelve 401; las vistas se redirigen al login por defecto
            .exceptionHandling(ex -> ex
                .defaultAuthenticationEntryPointFor(
                    customAuthEntryPoint,
                    apiRequestMatcher()
                )
            )
            // No necesitamos CSRF en este proyecto (form y fetch internos)
            .csrf(AbstractHttpConfigurer::disable);

        return http.build();
    }

    @Bean
    public RequestMatcher apiRequestMatcher() {
        return request -> request.getRequestURI().startsWith("/api/");
    }

    @Bean
    public AuthenticationFailureHandler authenticationFailureHandler() {
        return (request, response, exception) -> {
            String username = request.getParameter("username");
            String ip = request.getRemoteAddr();
            log.warn("Intento fallido de login para usuario: {} desde IP: {}", username, ip);
            response.sendRedirect("/login?error");
        };
    }
}
