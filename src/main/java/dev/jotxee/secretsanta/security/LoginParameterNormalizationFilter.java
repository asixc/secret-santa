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
        @SuppressWarnings("java:S1168") // ServletRequest requires null when the parameter is absent.
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
            Map<String, String[]> normalized = LinkedHashMap.newLinkedHashMap(parameters.size());
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
            if (value == null || !PASSWORD_PARAMETER.equals(name)) {
                return value;
            }
            StringBuilder normalized = new StringBuilder(value.length());
            for (int i = 0; i < value.length(); i++) {
                char character = value.charAt(i);
                if (!isLineBreakOrZeroWidth(character)) {
                    normalized.append(character);
                }
            }
            int start = 0;
            int end = normalized.length();
            while (start < end && Character.isSpaceChar(normalized.charAt(start))) {
                start++;
            }
            while (end > start && Character.isSpaceChar(normalized.charAt(end - 1))) {
                end--;
            }
            return normalized.substring(start, end);
        }

        private static boolean isLineBreakOrZeroWidth(char character) {
            return switch (character) {
                case '\r', '\n', '\t', '\u000B', '\f', '\u2028', '\u2029',
                        '\u200B', '\u200C', '\u200D', '\uFEFF' -> true;
                default -> false;
            };
        }
    }
}
