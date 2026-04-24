package com.malllite.agent.interceptor;

import com.malllite.common.exception.TooManyRequestsException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.Clock;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class AgentRateLimitInterceptor implements HandlerInterceptor {

    private final boolean enabled;
    private final int maxRequests;
    private final Duration window;
    private final Clock clock;
    private final Map<String, Deque<Long>> requestBuckets = new ConcurrentHashMap<>();

    @Autowired
    public AgentRateLimitInterceptor(
            @Value("${agent.rate-limit.enabled:true}") boolean enabled,
            @Value("${agent.rate-limit.max-requests:12}") int maxRequests,
            @Value("${agent.rate-limit.window-seconds:60}") long windowSeconds
    ) {
        this(enabled, maxRequests, windowSeconds, Clock.systemUTC());
    }

    AgentRateLimitInterceptor(boolean enabled, int maxRequests, long windowSeconds, Clock clock) {
        this.enabled = enabled;
        this.maxRequests = Math.max(1, maxRequests);
        this.window = Duration.ofSeconds(Math.max(1, windowSeconds));
        this.clock = clock;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!enabled) {
            return true;
        }

        String key = resolveClientKey(request);
        long now = clock.millis();
        long cutoff = now - window.toMillis();

        Deque<Long> timestamps = requestBuckets.computeIfAbsent(key, ignored -> new ArrayDeque<>());
        synchronized (timestamps) {
            while (!timestamps.isEmpty() && timestamps.peekFirst() < cutoff) {
                timestamps.removeFirst();
            }

            if (timestamps.size() >= maxRequests) {
                throw new TooManyRequestsException("Agent request rate limit exceeded. Please try again in a minute.");
            }

            timestamps.addLast(now);
        }

        return true;
    }

    private String resolveClientKey(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }

        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp.trim();
        }

        return request.getRemoteAddr();
    }
}
