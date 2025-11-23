package com.waters.punchout.gateway.filter;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.Semaphore;

@Slf4j
@Component
@Order(1)
public class RateLimitFilter implements Filter {

    private static final int MAX_REQUESTS_PER_SECOND = 100;
    private static final int PERMITS = MAX_REQUESTS_PER_SECOND;
    private final Semaphore semaphore = new Semaphore(PERMITS);
    private volatile long lastRefillTime = System.currentTimeMillis();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        refillPermits();

        if (semaphore.tryAcquire()) {
            try {
                chain.doFilter(request, response);
            } finally {
                schedulePermitRelease();
            }
        } else {
            handleRateLimitExceeded(httpRequest, httpResponse);
        }
    }

    private void refillPermits() {
        long currentTime = System.currentTimeMillis();
        long timeSinceLastRefill = currentTime - lastRefillTime;
        
        if (timeSinceLastRefill >= 1000) {
            int permitsToAdd = PERMITS - semaphore.availablePermits();
            if (permitsToAdd > 0) {
                semaphore.release(permitsToAdd);
            }
            lastRefillTime = currentTime;
        }
    }

    private void schedulePermitRelease() {
        new Thread(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    private void handleRateLimitExceeded(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        
        String clientIp = getClientIp(request);
        String requestUri = request.getRequestURI();
        
        log.warn("Rate limit exceeded - IP: {}, URI: {}, Method: {}", 
                clientIp, requestUri, request.getMethod());

        response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
        response.setContentType("application/json");
        response.getWriter().write(
            "{\"error\":\"Rate limit exceeded\",\"message\":\"Too many requests. Please try again later.\",\"retryAfter\":1}"
        );
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
}
