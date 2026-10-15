package br.edu.ucsal.clima.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.web.cors.CorsUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

public class FrontendOriginGuardFilter extends OncePerRequestFilter {
    private final String frontendOrigin;
    private final String frontendHeader;

    public FrontendOriginGuardFilter(String frontendOrigin, String frontendHeader) {
        this.frontendOrigin = frontendOrigin;
        this.frontendHeader = frontendHeader;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String origin = request.getHeader(HttpHeaders.ORIGIN);
        boolean originPermitida = origin == null || frontendOrigin.equals(origin);
        boolean preflightPermitido = CorsUtils.isPreFlightRequest(request) && frontendOrigin.equals(origin);
        boolean headerPresente = frontendHeader.equals(request.getHeader("X-Requested-By"));

        if (!originPermitida || (!headerPresente && !preflightPermitido)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json");
            response.getWriter().write("{\"erro\":\"Acesso negado\"}");
            return;
        }
        chain.doFilter(request, response);
    }
}
