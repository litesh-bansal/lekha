package com.lekha.langchain.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Clears the MDC once the whole request is done. Handlers populate it, but they must not
 * clear it themselves: a controller's {@code finally} runs before @RestControllerAdvice,
 * which would leave error responses without their correlation id. Clearing here also
 * stops values leaking onto the next request to reuse the pooled thread.
 */
@Component
public class MdcCleanupFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }
}
