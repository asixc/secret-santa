package dev.jotxee.secretsanta.config;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.util.matcher.RequestMatcher;

import jakarta.servlet.ServletException;
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

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Autorizaciones: sólo panel y API requieren login
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/", "/login", "/error",
                    "/css/**", "/js/**", "/images/**", "/audio/**", "/favicon.ico"
                ).permitAll()
                .requestMatchers("/create/**", "/api/**").authenticated()
                .anyRequest().permitAll()
            )
            // Login por formulario para vistas, con handler de error
            .formLogin(form -> form
                .loginPage("/login")
                .permitAll()
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
            .csrf(csrf -> csrf.disable());

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
                    throws IOException, ServletException {
                String username = request.getParameter("username");
                String ip = request.getRemoteAddr();
                log.warn("Intento fallido de login para usuario: {} desde IP: {}", username, ip);
                response.sendRedirect("/login?error");
            }
        };
    }

    @Bean
    public UserDetailsService users() {
        UserDetails user = User.withUsername(adminUser)
            .password("{noop}" + adminPassword)
            .roles("ADMIN")
            .build();
        return new InMemoryUserDetailsManager(user);
    }
}
