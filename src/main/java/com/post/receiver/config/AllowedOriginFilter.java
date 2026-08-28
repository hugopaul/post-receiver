package com.post.receiver.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class AllowedOriginFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(AllowedOriginFilter.class);
    private static final String HEALTH_PATH = "/api/health";

    private final CorsProperties corsProperties;

    public AllowedOriginFilter(CorsProperties corsProperties) {
        this.corsProperties = corsProperties;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (isHealthCheck(request) || isPermitted(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        log.warn(
                "Requisição rejeitada: origem não permitida | {} {} | origin={} | referer={} | ip={} | user-agent={}",
                request.getMethod(),
                request.getRequestURI(),
                blankToDash(request.getHeader("Origin")),
                blankToDash(request.getHeader("Referer")),
                request.getRemoteAddr(),
                blankToDash(request.getHeader("User-Agent"))
        );
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write("{\"status\":\"error\",\"message\":\"Origem não permitida\"}");
    }

    private boolean isHealthCheck(HttpServletRequest request) {
        return HEALTH_PATH.equals(request.getRequestURI());
    }

    private boolean isPermitted(HttpServletRequest request) {
        List<String> allowedOrigins = corsProperties.getAllowedOrigins();
        if (allowedOrigins == null || allowedOrigins.isEmpty()) {
            return true;
        }

        String origin = request.getHeader("Origin");
        if (origin != null && allowedOrigins.stream().anyMatch(allowed -> originEquals(allowed, origin))) {
            return true;
        }

        String referer = request.getHeader("Referer");
        if (referer != null && allowedOrigins.stream().anyMatch(referer::startsWith)) {
            return true;
        }

        String userAgent = request.getHeader("User-Agent");
        return userAgent != null && allowedOrigins.stream().anyMatch(userAgent::contains);
    }

    private static boolean originEquals(String allowed, String origin) {
        return allowed.equals(origin) || (allowed + "/").equals(origin);
    }

    private static String blankToDash(String value) {
        return (value == null || value.isBlank()) ? "-" : value;
    }
}
