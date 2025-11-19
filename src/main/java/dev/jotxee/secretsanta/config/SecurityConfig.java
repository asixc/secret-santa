package dev.jotxee.secretsanta.config;

import java.io.IOException;

import dev.jotxee.secretsanta.security.ParticipanteUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.util.matcher.RequestMatcher;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class SecurityConfig {

    @Value("${ADMIN_USER:admin}")
    private String adminUser;

    @Value("${ADMIN_PASSWORD:adminpassword}")
    private String adminPassword;

    @Autowired
    private CustomAuthEntryPoint customAuthEntryPoint;

    @Autowired
    private ParticipanteUserDetailsService participanteUserDetailsService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Bean
    public DaoAuthenticationProvider participanteAuthenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(participanteUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public InMemoryUserDetailsManager adminUserDetailsManager() {
        UserDetails admin = User.withUsername(adminUser)
            .password("{noop}" + adminPassword)
            .roles("ADMIN")
            .build();
        return new InMemoryUserDetailsManager(admin);
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        // Prioridad 1: Usuarios participantes (con BCrypt)
        auth.authenticationProvider(participanteAuthenticationProvider());
        // Prioridad 2: Admin (noop - sin cifrado)
        auth.userDetailsService(adminUserDetailsManager());
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Autorizaciones
            .authorizeHttpRequests(auth -> auth
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
                    apiAwareEntryPoint(),
                    apiRequestMatcher()
                )
            )
            // No necesitamos CSRF en este proyecto (form y fetch internos)
            .csrf(AbstractHttpConfigurer::disable);

        return http.build();
    }

    @Bean
    public CustomAuthEntryPoint apiAwareEntryPoint() {
        return customAuthEntryPoint;
    }

    @Bean
    public RequestMatcher apiRequestMatcher() {
        return request -> request.getRequestURI().startsWith("/api/");
    }

    @Bean
    public AuthenticationFailureHandler authenticationFailureHandler() {
        return new AuthenticationFailureHandler() {
            @Override
            public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                                org.springframework.security.core.AuthenticationException exception)
                    throws IOException {
                String username = request.getParameter("username");
                String ip = request.getRemoteAddr();
                log.warn("Intento fallido de login para usuario: {} desde IP: {}", username, ip);
                response.sendRedirect("/login?error");
            }
        };
    }
}
