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
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
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

    @Value("${admin.user:admin}")
    private String adminUser;

    @Value("${admin.password:adminpassword}")
    private String adminPassword;

    @Bean
    public DaoAuthenticationProvider participanteAuthenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(participanteUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public DaoAuthenticationProvider adminAuthenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(adminUserDetailsManager());
        // No usa password encoder porque las contraseñas están en texto plano
        provider.setPasswordEncoder(NoOpPasswordEncoder.getInstance());
        return provider;
    }

    @Bean
    public InMemoryUserDetailsManager adminUserDetailsManager() {
        // Validar que las credenciales no estén vacías y usar defaults si es necesario
        String username = adminUser;
        String password = adminPassword;
        
        if (username == null || username.trim().isEmpty()) {
            log.warn("ADMIN_USER is null or empty. Using default 'admin'");
            username = "admin";
        }
        if (password == null || password.trim().isEmpty()) {
            log.warn("ADMIN_PASSWORD is null or empty. Using default 'adminpassword'");
            password = "adminpassword";
        }

        UserDetails admin = User.withUsername(username)
            .password(password)  // Sin {noop} porque usamos NoOpPasswordEncoder
            .roles("ADMIN")
            .build();
        return new InMemoryUserDetailsManager(admin);
    }

    @Bean
    public AuthenticationManager authenticationManager() {
        // Crear un AuthenticationManager con ambos providers
        return new ProviderManager(List.of(
            adminAuthenticationProvider(),
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
