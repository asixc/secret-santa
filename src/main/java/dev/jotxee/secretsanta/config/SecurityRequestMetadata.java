package dev.jotxee.secretsanta.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

@Component
public class SecurityRequestMetadata {

    public static final String REQUEST_ID_ATTRIBUTE = SecurityRequestMetadata.class.getName() + ".requestId";

    private static final int MAX_VALUE_LENGTH = 512;

    private final SecurityLoggingProperties properties;

    public SecurityRequestMetadata(SecurityLoggingProperties properties) {
        this.properties = properties;
    }

    public String requestId(HttpServletRequest request) {
        Object existingRequestId = request.getAttribute(REQUEST_ID_ATTRIBUTE);
        if (existingRequestId instanceof String requestId && !requestId.isBlank()) {
            return requestId;
        }

        String requestId = UUID.randomUUID().toString();
        request.setAttribute(REQUEST_ID_ATTRIBUTE, requestId);
        return requestId;
    }

    public String method(HttpServletRequest request) {
        return display(request.getMethod());
    }

    public String uri(HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        if (requestUri != null) {
            int queryStart = requestUri.indexOf('?');
            int fragmentStart = requestUri.indexOf('#');
            int end = requestUri.length();
            if (queryStart >= 0) {
                end = Math.min(end, queryStart);
            }
            if (fragmentStart >= 0) {
                end = Math.min(end, fragmentStart);
            }
            requestUri = requestUri.substring(0, end);
        }
        return display(requestUri);
    }

    public String userAgent(HttpServletRequest request) {
        return display(request.getHeader("User-Agent"));
    }

    public String referer(HttpServletRequest request) {
        String referer = request.getHeader("Referer");
        if (referer != null) {
            int queryStart = referer.indexOf('?');
            int fragmentStart = referer.indexOf('#');
            int end = referer.length();
            if (queryStart >= 0) {
                end = Math.min(end, queryStart);
            }
            if (fragmentStart >= 0) {
                end = Math.min(end, fragmentStart);
            }
            referer = referer.substring(0, end);
        }
        return display(referer);
    }

    public String accept(HttpServletRequest request) {
        return display(request.getHeader("Accept"));
    }

    public String clientIp(HttpServletRequest request) {
        return display(request.getRemoteAddr());
    }

    public String text(String value) {
        return display(value);
    }

    public String forwardedFor(HttpServletRequest request) {
        if (!properties.isTrustForwardedHeaders()) {
            return "-";
        }
        return display(request.getHeader("X-Forwarded-For"));
    }

    public String basicUsername(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null) {
            return "(no auth header)";
        }
        if (!authHeader.startsWith("Basic ")) {
            return "(non-basic auth)";
        }

        try {
            String encodedCredentials = authHeader.substring("Basic ".length());
            String credentials = new String(Base64.getDecoder().decode(encodedCredentials), StandardCharsets.UTF_8);
            int separator = credentials.indexOf(':');
            return separator > 0 ? display(credentials.substring(0, separator)) : "(malformed header)";
        } catch (IllegalArgumentException ignored) {
            return "(malformed header)";
        }
    }

    private String display(String value) {
        if (value == null || value.isBlank()) {
            return "-";
        }

        StringBuilder sanitized = new StringBuilder(Math.min(value.length(), MAX_VALUE_LENGTH));
        for (int index = 0; index < value.length() && sanitized.length() < MAX_VALUE_LENGTH; index++) {
            char character = value.charAt(index);
            if (Character.isISOControl(character) || character == '"' || character == '\\') {
                sanitized.append(' ');
            } else {
                sanitized.append(character);
            }
        }
        return sanitized.toString().trim();
    }
}
