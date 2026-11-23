package dev.jotxee.secretsanta.config;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SecurityRequestLoggingTests {

    @Test
    void doesNotReadForwardedForWithoutIngressTrustConfiguration() {
        SecurityRequestMetadata metadata = metadataWithForwardedHeadersTrust(false);
        MockHttpServletRequest request = request("GET", "/api/reveal");
        request.setRemoteAddr("198.51.100.20");
        request.addHeader("X-Forwarded-For", "203.0.113.10");

        assertEquals("-", metadata.forwardedFor(request));
    }

    @Test
    void readsForwardedForWhenIngressTrustIsConfigured() {
        SecurityRequestMetadata metadata = metadataWithForwardedHeadersTrust(true);
        MockHttpServletRequest request = request("GET", "/api/reveal");
        request.setRemoteAddr("203.0.113.10");
        request.addHeader("X-Forwarded-For", "203.0.113.10, 10.10.10.20");

        assertEquals("203.0.113.10, 10.10.10.20", metadata.forwardedFor(request));
    }

    @Test
    void omitsRefererQueryAndSanitizesControlCharacters() {
        SecurityRequestMetadata metadata = metadataWithForwardedHeadersTrust(false);
        MockHttpServletRequest request = request("GET", "/api/reveal?token=secret");
        request.addHeader("Referer", "https://example.test/page?token=secret#fragment");
        request.addHeader("User-Agent", "scanner\r\nforged");

        assertEquals("/api/reveal", metadata.uri(request));
        assertEquals("https://example.test/page", metadata.referer(request));
        assertEquals("scanner  forged", metadata.userAgent(request));
    }

    @Test
    void correlationFilterAddsResponseIdAndCleansMdc() throws Exception {
        SecurityRequestMetadata metadata = metadataWithForwardedHeadersTrust(false);
        RequestCorrelationFilter filter = new RequestCorrelationFilter(metadata);
        MockHttpServletRequest request = request("GET", "/");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicBoolean chainCalled = new AtomicBoolean();
        FilterChain chain = (filteredRequest, filteredResponse) -> {
            chainCalled.set(true);
            assertNotNull(filteredRequest.getAttribute(SecurityRequestMetadata.REQUEST_ID_ATTRIBUTE));
            assertNotNull(MDC.get("requestId"));
        };

        filter.doFilter(request, response, chain);

        String requestId = response.getHeader("X-Request-ID");
        assertTrue(chainCalled.get());
        assertNotNull(requestId);
        UUID.fromString(requestId);
        assertEquals(requestId, request.getAttribute(SecurityRequestMetadata.REQUEST_ID_ATTRIBUTE));
        assertNull(MDC.get("requestId"));
    }

    @Test
    void honeypotRequestReturnsNotFoundWithoutCallingApplication() throws Exception {
        SecurityRequestMetadata metadata = metadataWithForwardedHeadersTrust(false);
        SecurityRequestLogger logger = new SecurityRequestLogger(metadata);
        HoneypotRequestFilter filter = new HoneypotRequestFilter(logger);
        MockHttpServletRequest request = request("GET", "/.env");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicBoolean chainCalled = new AtomicBoolean();

        filter.doFilter(request, response, (filteredRequest, filteredResponse) -> chainCalled.set(true));

        assertFalse(chainCalled.get());
        assertEquals(404, response.getStatus());
    }

    @Test
    void normalRequestContinuesPastHoneypotFilter() throws Exception {
        SecurityRequestMetadata metadata = metadataWithForwardedHeadersTrust(false);
        SecurityRequestLogger logger = new SecurityRequestLogger(metadata);
        HoneypotRequestFilter filter = new HoneypotRequestFilter(logger);
        MockHttpServletRequest request = request("GET", "/public-page");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicBoolean chainCalled = new AtomicBoolean();

        filter.doFilter(request, response, (filteredRequest, filteredResponse) -> chainCalled.set(true));

        assertTrue(chainCalled.get());
    }

    private SecurityRequestMetadata metadataWithForwardedHeadersTrust(boolean trustForwardedHeaders) {
        SecurityLoggingProperties properties = new SecurityLoggingProperties();
        properties.setTrustForwardedHeaders(trustForwardedHeaders);
        return new SecurityRequestMetadata(properties);
    }

    private MockHttpServletRequest request(String method, String uri) {
        return new MockHttpServletRequest(method, uri);
    }
}
