package com.post.receiver.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestOriginLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RequestOriginLoggingFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        log.info(
                "Requisição recebida: {} {} | origin={} | referer={} | ip={} | forwarded={} | host={} | user-agent={}",
                request.getMethod(),
                request.getRequestURI(),
                header(request, "Origin"),
                header(request, "Referer"),
                request.getRemoteAddr(),
                firstForwardedIp(request),
                header(request, "Host"),
                header(request, "User-Agent")
        );
        filterChain.doFilter(request, response);
    }

    private static String header(HttpServletRequest request, String name) {
        String value = request.getHeader(name);
        return (value == null || value.isBlank()) ? "-" : value;
    }

    private static String firstForwardedIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded == null || forwarded.isBlank()) {
            String realIp = request.getHeader("X-Real-IP");
            return (realIp == null || realIp.isBlank()) ? "-" : realIp.trim();
        }
        int comma = forwarded.indexOf(',');
        String first = comma >= 0 ? forwarded.substring(0, comma) : forwarded;
        return first.trim();
    }
}
