package dev.jotxee.secretsanta.config;

import dev.jotxee.secretsanta.security.LoginParameterNormalizationFilter;
import dev.jotxee.secretsanta.security.ParticipanteUserDetails;
import dev.jotxee.secretsanta.security.ParticipanteUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import java.util.List;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private static final String LOGIN_PROCESSING_URL = "/login";

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
    public SecurityFilterChain filterChain(HttpSecurity http) {
        http
            // Autorizaciones
            .authorizeHttpRequests(authz -> authz
                .requestMatchers(
                    "/", LOGIN_PROCESSING_URL, "/error",
                    "/css/**", "/js/**", "/images/**", "/audio/**", "/favicon.ico", "/favicon.svg"
                ).permitAll()
                .requestMatchers("/create/**", "/api/**").hasRole("ADMIN")
                .requestMatchers("/my-profile/**").hasRole("USER")
                .anyRequest().permitAll()
            )
            // Login por formulario con redirección condicional
            .formLogin(form -> form
                .loginPage(LOGIN_PROCESSING_URL)
                .loginProcessingUrl(LOGIN_PROCESSING_URL)
                .permitAll()
                .successHandler((request, response, authentication) -> {
                    // Get the user's name instead of email for logging
                    String userName = authentication.getPrincipal() instanceof ParticipanteUserDetails userDetails
                            ? userDetails.getUsuario().getNombre()
                            : "usuario";
                    log.info("Login exitoso para: {}", userName);
                    if (authentication.getAuthorities().stream()
                            .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()))) {
                        response.sendRedirect("/create");
                    } else {
                        response.sendRedirect("/my-profile");
                    }
                })
                .failureHandler(authenticationFailureHandler())
            )
            .addFilterBefore(new LoginParameterNormalizationFilter(
                    PathPatternRequestMatcher.withDefaults().matcher(HttpMethod.POST, LOGIN_PROCESSING_URL)),
                    UsernamePasswordAuthenticationFilter.class)
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
