package dev.jotxee.secretsanta.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class LoginParameterNormalizationFilter extends OncePerRequestFilter {

    private static final String PASSWORD_PARAMETER = "password";
    private static final String INVISIBLE_WHITESPACE = "[\\s\\p{Z}\\u200B\\uFEFF]";
    private static final String LINE_BREAKS_AND_ZERO_WIDTH = "[\\r\\n\\t\\u000B\\f\\u2028\\u2029\\u200B\\u200C\\u200D\\uFEFF]+";

    private final RequestMatcher loginMatcher;

    public LoginParameterNormalizationFilter(RequestMatcher loginMatcher) {
        this.loginMatcher = loginMatcher;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !loginMatcher.matches(request);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        filterChain.doFilter(new NormalizedLoginRequest(request), response);
    }

    private static final class NormalizedLoginRequest extends HttpServletRequestWrapper {

        private NormalizedLoginRequest(HttpServletRequest request) {
            super(request);
        }

        @Override
        public String getParameter(String name) {
            return normalize(name, super.getParameter(name));
        }

        @Override
        public String[] getParameterValues(String name) {
            String[] values = super.getParameterValues(name);
            if (values == null) {
                return null;
            }
            String[] normalized = new String[values.length];
            for (int i = 0; i < values.length; i++) {
                normalized[i] = normalize(name, values[i]);
            }
            return normalized;
        }

        @Override
        public Map<String, String[]> getParameterMap() {
            Map<String, String[]> parameters = super.getParameterMap();
            Map<String, String[]> normalized = new LinkedHashMap<>(parameters.size());
            parameters.forEach((name, values) -> {
                String[] copy = new String[values.length];
                for (int i = 0; i < values.length; i++) {
                    copy[i] = normalize(name, values[i]);
                }
                normalized.put(name, copy);
            });
            return Collections.unmodifiableMap(normalized);
        }

        private static String normalize(String name, String value) {
            if (value == null) {
                return null;
            }
            if (PASSWORD_PARAMETER.equals(name)) {
                return value
                        .replaceAll(LINE_BREAKS_AND_ZERO_WIDTH, "")
                        .replaceAll("^" + INVISIBLE_WHITESPACE + "+|" + INVISIBLE_WHITESPACE + "+$", "");
            }
            return value;
        }
    }
}
