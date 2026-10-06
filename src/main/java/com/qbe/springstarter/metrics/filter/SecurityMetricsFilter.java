package com.qbe.springstarter.metrics.filter;

import com.qbe.springstarter.constants.MetricsConstants;
import com.qbe.springstarter.metrics.SampleMetrics;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class SecurityMetricsFilter extends OncePerRequestFilter {

    private static final String SAMPLES = "/api/v1/samples";
    private static final String UNKNOWN = "unknown";

    private final SampleMetrics sampleMetrics;

    public SecurityMetricsFilter(SampleMetrics sampleMetrics) {
        this.sampleMetrics = sampleMetrics;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        try {
            filterChain.doFilter(request, response);
        } finally {
            recordSecurityError(request, response);
        }
    }

    private void recordSecurityError(HttpServletRequest request, HttpServletResponse response) {
        String status =
                switch (response.getStatus()) {
                    case HttpServletResponse.SC_UNAUTHORIZED -> SampleMetrics.UNAUTHORIZED;
                    case HttpServletResponse.SC_FORBIDDEN -> SampleMetrics.FORBIDDEN;
                    default -> null;
                };

        if (status == null) {
            return;
        }

        sampleMetrics.increment(resolveOperation(request), status);
    }

    private String resolveOperation(HttpServletRequest request) {
        String path = getApplicationPath(request);
        String method = request.getMethod();

        if (SAMPLES.equals(path)) {
            return switch (method) {
                case "POST" -> MetricsConstants.CREATE;
                case "GET" -> MetricsConstants.FIND_ALL;
                default -> UNKNOWN;
            };
        }

        if (path.matches("^" + SAMPLES + "/[^/]+$")) {
            return switch (method) {
                case "GET" -> MetricsConstants.FIND_BY_ID;
                case "PUT" -> MetricsConstants.UPDATE;
                case "DELETE" -> MetricsConstants.DELETE;
                default -> UNKNOWN;
            };
        }
        return UNKNOWN;
    }

    private String getApplicationPath(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String contextPath = request.getContextPath();

        if (!contextPath.isEmpty() && uri.startsWith(contextPath)) {
            return uri.substring(contextPath.length());
        }
        return uri;
    }
}
