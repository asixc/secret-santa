package dev.jotxee.secretsanta.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Base64;

@Slf4j
@Component
public class CustomAuthEntryPoint implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
        String authHeader = request.getHeader("Authorization");
        String username = "(no auth header)";
        if (authHeader != null && authHeader.startsWith("Basic ")) {
            try {
                String base64Credentials = authHeader.substring("Basic ".length());
                String credentials = new String(Base64.getDecoder().decode(base64Credentials));
                int idx = credentials.indexOf(":");
                if (idx > 0) {
                    username = credentials.substring(0, idx);
                }
            } catch (Exception _) {
                username = "(malformed header)";
            }
        }
        log.warn("[SECURITY] Failed login attempt. Username: '" + username + "' IP: " + request.getRemoteAddr());
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, authException.getMessage());
    }
}
