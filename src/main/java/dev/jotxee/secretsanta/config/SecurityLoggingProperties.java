package dev.jotxee.secretsanta.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "security.logging")
public class SecurityLoggingProperties {

    /**
     * Traefik is the only public ingress and sanitizes forwarded headers before
     * forwarding requests to the ClusterIP service.
     */
    private boolean trustForwardedHeaders;

    public boolean isTrustForwardedHeaders() {
        return trustForwardedHeaders;
    }

    public void setTrustForwardedHeaders(boolean trustForwardedHeaders) {
        this.trustForwardedHeaders = trustForwardedHeaders;
    }
}
