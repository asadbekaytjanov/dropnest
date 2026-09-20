package uz.aytjanov.dropnest.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitFilter extends OncePerRequestFilter {
    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();
    private Bucket newBucket(String method) {
        int capacity;
        int refillTokens;

        if ("POST".equalsIgnoreCase(method)) {
            capacity = 5;
            refillTokens = 5;
        } else if ("DELETE".equalsIgnoreCase(method)) {
            capacity = 3;
            refillTokens = 3;
        } else if ("GET".equalsIgnoreCase(method)) {
            capacity = 100;
            refillTokens = 100;
        } else {
            capacity = 20;
            refillTokens = 20;
        }
        return Bucket.builder()
                .addLimit(
                        Bandwidth.classic(
                                capacity,
                                Refill.intervally(
                                        refillTokens,
                                        Duration.ofMinutes(1)
                                )
                        )
                )
                .build();
    }
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {
        String path = request.getRequestURI();
        String method = request.getMethod();
        Authentication auth =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        String actor =
                auth != null
                        && auth.isAuthenticated()
                        && !"anonymousUser".equals(auth.getName())
                        ? "user:" + auth.getName()
                        : "ip:" + request.getRemoteAddr();

        String key = method + "|" + path + "|" + actor;
        Bucket bucket = cache.computeIfAbsent(
                key,
                k -> newBucket(method)
        );
        if (!bucket.tryConsume(1)) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"TOO_MANY_REQUESTS\",\"message\":\"Rate limit exceeded\"}");
            return;
        }
        filterChain.doFilter(request, response);
    }
}