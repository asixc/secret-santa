package dev.jotxee.secretsanta.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Locale;
import java.util.Set;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class HoneypotRequestFilter extends OncePerRequestFilter {

    private static final Set<String> EXACT_PATHS = Set.of(
            "/.env",
            "/.env.local",
            "/.env.production",
            "/.env.example",
            "/wp-admin",
            "/wp-login.php",
            "/phpmyadmin",
            "/server-status"
    );

    private final SecurityRequestLogger securityRequestLogger;

    public HoneypotRequestFilter(SecurityRequestLogger securityRequestLogger) {
        this.securityRequestLogger = securityRequestLogger;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        if (!isHoneypotPath(request.getRequestURI())) {
            filterChain.doFilter(request, response);
            return;
        }

        securityRequestLogger.honeypotRequest(request, HttpServletResponse.SC_NOT_FOUND);
        response.sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    private boolean isHoneypotPath(String requestUri) {
        if (requestUri == null) {
            return false;
        }

        String path = requestUri.toLowerCase(Locale.ROOT);
        return EXACT_PATHS.contains(path)
                || path.startsWith("/.git/")
                || path.startsWith("/.svn/")
                || path.startsWith("/.hg/")
                || path.startsWith("/phpmyadmin/");
    }
}
