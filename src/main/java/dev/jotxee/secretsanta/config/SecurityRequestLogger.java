package dev.jotxee.secretsanta.config;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SecurityRequestLogger {

    private final SecurityRequestMetadata metadata;

    public SecurityRequestLogger(SecurityRequestMetadata metadata) {
        this.metadata = metadata;
    }

    public void unauthorizedApiRequest(HttpServletRequest request, int status) {
        log.warn(
                "[SECURITY] Unauthorized API request security_event=unauthorized_api_request " +
                        "request_id={} status={} method={} uri={} username={} client_ip={} forwarded_for={} " +
                        "user_agent=\"{}\" referer=\"{}\" accept=\"{}\"",
                metadata.requestId(request), status, metadata.method(request), metadata.uri(request),
                metadata.basicUsername(request), metadata.clientIp(request), metadata.forwardedFor(request),
                metadata.userAgent(request), metadata.referer(request), metadata.accept(request)
        );
    }

    public void failedLoginAttempt(HttpServletRequest request, int status, String username) {
        log.warn(
                "[SECURITY] Failed login attempt security_event=failed_login_attempt " +
                        "request_id={} status={} method={} uri={} username=\"{}\" client_ip={} forwarded_for={} " +
                        "user_agent=\"{}\" referer=\"{}\" accept=\"{}\"",
                metadata.requestId(request), status, metadata.method(request), metadata.uri(request),
                metadata.text(username), metadata.clientIp(request), metadata.forwardedFor(request),
                metadata.userAgent(request), metadata.referer(request), metadata.accept(request)
        );
    }

    public void honeypotRequest(HttpServletRequest request, int status) {
        log.warn(
                "[SECURITY] Honeypot request security_event=honeypot_request " +
                        "request_id={} status={} method={} uri={} client_ip={} forwarded_for={} " +
                        "user_agent=\"{}\" referer=\"{}\" accept=\"{}\"",
                metadata.requestId(request), status, metadata.method(request), metadata.uri(request),
                metadata.clientIp(request), metadata.forwardedFor(request), metadata.userAgent(request),
                metadata.referer(request), metadata.accept(request)
        );
    }
}
