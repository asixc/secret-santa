package dev.jotxee.secretsanta.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class LoginParameterNormalizationFilterTest {

    private final LoginParameterNormalizationFilter filter = new LoginParameterNormalizationFilter();

    @Test
    void normalizesLoginParameters() throws Exception {
        MockHttpServletRequest request = loginRequest(" user @example.com ", " Abc 123\n!@#\u00A0");

        HttpServletRequest filtered = filter(request);

        assertThat(filtered.getParameter("username")).isEqualTo("user@example.com");
        assertThat(filtered.getParameter("password")).isEqualTo("Abc 123!@#");
    }

    @Test
    void preservesInternalSpacesInPassword() throws Exception {
        MockHttpServletRequest request = loginRequest("user@example.com", "frase secreta");

        HttpServletRequest filtered = filter(request);

        assertThat(filtered.getParameter("password")).isEqualTo("frase secreta");
    }

    @Test
    void normalizesParameterValuesAndMap() throws Exception {
        MockHttpServletRequest request = loginRequest("user@example.com", " Abc 123 ");
        request.addParameter("password", "Otra\t456");

        HttpServletRequest filtered = filter(request);

        assertThat(filtered.getParameterValues("password")).containsExactly("Abc 123", "Otra456");
        assertThat(filtered.getParameterMap().get("password")).containsExactly("Abc 123", "Otra456");
    }

    @Test
    void leavesNonLoginRequestsUntouched() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/create");
        request.setServletPath("/create");
        request.addParameter("password", " Abc 123 ");

        HttpServletRequest filtered = filter(request);

        assertThat(filtered.getParameter("password")).isEqualTo(" Abc 123 ");
    }

    @Test
    void leavesGetLoginRequestsUntouched() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/login");
        request.setServletPath("/login");
        request.addParameter("password", " Abc 123 ");

        HttpServletRequest filtered = filter(request);

        assertThat(filtered.getParameter("password")).isEqualTo(" Abc 123 ");
    }

    private MockHttpServletRequest loginRequest(String username, String password) {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/login");
        request.setServletPath("/login");
        request.addParameter("username", username);
        request.addParameter("password", password);
        return request;
    }

    private HttpServletRequest filter(MockHttpServletRequest request) throws Exception {
        AtomicReference<HttpServletRequest> filteredRequest = new AtomicReference<>();
        FilterChain chain = (filtered, response) -> filteredRequest.set((HttpServletRequest) filtered);

        filter.doFilter(request, new MockHttpServletResponse(), chain);

        return filteredRequest.get();
    }
}
