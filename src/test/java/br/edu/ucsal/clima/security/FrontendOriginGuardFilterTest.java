package br.edu.ucsal.clima.security;

import jakarta.servlet.ServletException;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import java.io.IOException;
import static org.assertj.core.api.Assertions.assertThat;

class FrontendOriginGuardFilterTest {
    private final FrontendOriginGuardFilter filter =
            new FrontendOriginGuardFilter("http://localhost:4200", "Clima-Angular-SPA");

    @Test
    void rejeitaAcessoDiretoSemHeader() throws ServletException, IOException {
        var request = new MockHttpServletRequest("GET", "/api/auth/me");
        var response = new MockHttpServletResponse();
        filter.doFilter(request, response, new MockFilterChain());
        assertThat(response.getStatus()).isEqualTo(403);
    }

    @Test
    void aceitaHeaderDaInterfaceSemOriginComoNoProxy() throws ServletException, IOException {
        var request = new MockHttpServletRequest("GET", "/api/auth/me");
        request.addHeader("X-Requested-By", "Clima-Angular-SPA");
        var response = new MockHttpServletResponse();
        filter.doFilter(request, response, new MockFilterChain());
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    void rejeitaOriginEstranhaMesmoComHeaderCorreto() throws ServletException, IOException {
        var request = new MockHttpServletRequest("GET", "/api/auth/me");
        request.addHeader("X-Requested-By", "Clima-Angular-SPA");
        request.addHeader("Origin", "http://outro-site.test");
        var response = new MockHttpServletResponse();
        filter.doFilter(request, response, new MockFilterChain());
        assertThat(response.getStatus()).isEqualTo(403);
    }

    @Test
    void aceitaPreflightDaOrigemPermitidaSemHeaderAplicadoAinda() throws ServletException, IOException {
        var request = new MockHttpServletRequest("OPTIONS", "/api/auth/login");
        request.addHeader("Origin", "http://localhost:4200");
        request.addHeader("Access-Control-Request-Method", "POST");
        request.addHeader("Access-Control-Request-Headers", "x-requested-by,content-type");
        var response = new MockHttpServletResponse();
        filter.doFilter(request, response, new MockFilterChain());
        assertThat(response.getStatus()).isEqualTo(200);
    }
}
