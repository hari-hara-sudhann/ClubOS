package com.codeygen.clubos.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.regex.Pattern;

@Component
public class HttpExchangeLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(HttpExchangeLoggingFilter.class);
    private static final int MAX_PAYLOAD_LENGTH = 2000;
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("(?i)(\"(?:rawPassword|password|passwordHash)\"\\s*:\\s*\")[^\"]*(\")");
    private static final Pattern JWT_PATTERN = Pattern.compile("(?i)(\"jwt\"\\s*:\\s*\")[^\"]*(\")");

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/swagger-resources")
                || path.startsWith("/favicon");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request, MAX_PAYLOAD_LENGTH);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);
        String requestId = UUID.randomUUID().toString();
        Instant start = Instant.now();

        wrappedResponse.setHeader("X-Request-Id", requestId);

        try {
            filterChain.doFilter(wrappedRequest, wrappedResponse);
        } finally {
            long durationMs = Duration.between(start, Instant.now()).toMillis();
            String requestBody = extractPayload(wrappedRequest.getContentAsByteArray());
            String responseBody = extractPayload(wrappedResponse.getContentAsByteArray());
            String query = request.getQueryString() == null ? "" : "?" + request.getQueryString();

            log.info(
                    "HTTP EXCHANGE [{}]: method={} path={}{} status={} duration={}ms",
                    requestId,
                    request.getMethod(),
                    request.getRequestURI(),
                    query,
                    wrappedResponse.getStatus(),
                    durationMs
            );

            if (wrappedResponse.getStatus() >= 400 || request.getRequestURI().contains("/oauth2/")) {
                log.info("Request Body [{}]: {}", requestId, requestBody);
                log.info("Response Body [{}]: {}", requestId, responseBody);
            }

            wrappedResponse.copyBodyToResponse();
        }
    }

    private String extractPayload(byte[] content) {
        if (content == null || content.length == 0) {
            return "<empty>";
        }

        String payload = new String(content, StandardCharsets.UTF_8);
        payload = PASSWORD_PATTERN.matcher(payload).replaceAll("$1***$2");
        payload = JWT_PATTERN.matcher(payload).replaceAll("$1***$2");

        if (payload.length() > MAX_PAYLOAD_LENGTH) {
            return payload.substring(0, MAX_PAYLOAD_LENGTH) + "...<truncated>";
        }
        return payload;
    }
}
