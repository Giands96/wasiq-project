package com.wasiq.inmobiliaria.security.ratelimit;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.UrlPathHelper;

import java.io.IOException;
import java.time.Duration;
import java.util.List;

/**
 * Per-IP limits for abuse-prone endpoints.
 * Not a Spring bean on purpose: it is registered only inside the security chain (after CORS),
 * so 429 responses still carry CORS headers and the browser can read them.
 */
public class RateLimitFilter extends OncePerRequestFilter {

    private static final List<Rule> RULES = List.of(
            new Rule("POST", "/auth/login", false, 10, Duration.ofMinutes(1)),
            new Rule("POST", "/auth/register", false, 5, Duration.ofHours(1)),
            new Rule("POST", "/auth/refresh", false, 60, Duration.ofMinutes(1)),
            new Rule("POST", "/properties/create", false, 30, Duration.ofHours(1)),
            new Rule("PUT", "/properties/update/", true, 30, Duration.ofHours(1))
    );

    private final RateLimiter rateLimiter;

    public RateLimitFilter(RateLimiter rateLimiter) {
        this.rateLimiter = rateLimiter;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        //* Decoded path, so "/auth/%6Cogin" cannot bypass the "/auth/login" rule
        String path = UrlPathHelper.defaultInstance.getPathWithinApplication(request);

        for (Rule rule : RULES) {
            if (rule.matches(request.getMethod(), path)) {
                //* getRemoteAddr is the real client IP only when server.forward-headers-strategy matches the deployment
                String key = rule.path() + ":" + request.getRemoteAddr();
                if (!rateLimiter.tryConsume(key, rule.limit(), rule.window())) {
                    reject(response, rule);
                    return;
                }
                break;
            }
        }

        filterChain.doFilter(request, response);
    }

    private void reject(HttpServletResponse response, Rule rule) throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setHeader("Retry-After", String.valueOf(rule.window().toSeconds()));
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(
                "{\"status\":429,\"message\":\"Demasiadas solicitudes, intenta nuevamente mas tarde\"}");
    }

    private record Rule(String method, String path, boolean prefix, int limit, Duration window) {
        boolean matches(String requestMethod, String requestPath) {
            if (!method.equals(requestMethod)) {
                return false;
            }
            return prefix ? requestPath.startsWith(path) : requestPath.equals(path);
        }
    }
}
