package com.adeptview.expenseportal.common;

import java.io.IOException;
import java.util.UUID;
import java.util.regex.Pattern;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Gives every request an ID that appears on each log line it produces, in the
 * {@code X-Request-Id} response header and in every error body.
 *
 * A caller-supplied ID is kept when it looks like one, so a request can be
 * followed through a proxy. Anything else is replaced rather than echoed.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestIdFilter extends OncePerRequestFilter {

    public static final String HEADER = "X-Request-Id";
    public static final String MDC_KEY = "request_id";

    private static final Pattern ACCEPTED = Pattern.compile("[A-Za-z0-9-]{8,64}");

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String requestId = incomingOrNew(request.getHeader(HEADER));
        MDC.put(MDC_KEY, requestId);
        response.setHeader(HEADER, requestId);
        try {
            chain.doFilter(request, response);
        } finally {
            MDC.remove(MDC_KEY);
        }
    }

    static String incomingOrNew(String incoming) {
        if (incoming != null && ACCEPTED.matcher(incoming).matches()) {
            return incoming;
        }
        return UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }
}
