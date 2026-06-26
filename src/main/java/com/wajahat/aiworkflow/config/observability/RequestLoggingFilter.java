package com.wajahat.aiworkflow.config.observability;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestLoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        if (isAsyncDispatch(request)) {
            filterChain.doFilter(request, response);
        } else {
            ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request, 1024);
            ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);
            doFilterWrapped(wrappedRequest, wrappedResponse, filterChain);
        }
    }

    protected void doFilterWrapped(ContentCachingRequestWrapper request, ContentCachingResponseWrapper response, FilterChain filterChain)
            throws ServletException, IOException {
        long startTime = System.currentTimeMillis();
        try {
            filterChain.doFilter(request, response);
        } finally {
            logRequest(request, response, System.currentTimeMillis() - startTime);
            response.copyBodyToResponse();
        }
    }

    private void logRequest(ContentCachingRequestWrapper request, ContentCachingResponseWrapper response, long duration) {
        int status = response.getStatus();
        String method = request.getMethod();
        String uri = request.getRequestURI();
        String queryString = request.getQueryString();
        String remoteAddr = request.getRemoteAddr();

        if (queryString != null) {
            uri += "?" + queryString;
        }

        log.info("Request: method={}, uri={}, status={}, duration={}ms, remote={}, payload={}",
                method, uri, status, duration, remoteAddr, getPayload(request));
    }

    private String getPayload(ContentCachingRequestWrapper request) {
        byte[] buf = request.getContentAsByteArray();
        if (buf.length > 0) {
            String encoding = request.getCharacterEncoding();
            try {
                return new String(buf, 0, Math.min(buf.length, 1024), encoding != null ? encoding : "UTF-8");
            } catch (UnsupportedEncodingException ex) {
                return "[unknown]";
            }
        }
        return "";
    }
}
