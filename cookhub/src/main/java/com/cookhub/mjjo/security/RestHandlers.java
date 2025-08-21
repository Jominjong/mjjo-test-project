package com.cookhub.mjjo.security;

import jakarta.servlet.http.*;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.*;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class RestHandlers {
    public static class JsonAuthEntryPoint implements AuthenticationEntryPoint {
        @Override public void commence(HttpServletRequest req, HttpServletResponse res, AuthenticationException e) throws IOException {
            res.setStatus(401);
            res.setContentType(MediaType.APPLICATION_JSON_VALUE);
            res.setCharacterEncoding(StandardCharsets.UTF_8.name());
            res.getWriter().write("{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"인증이 필요합니다.\",\"path\":\""+req.getRequestURI()+"\"}");
        }
    }
    public static class JsonAccessDeniedHandler implements AccessDeniedHandler {
        @Override public void handle(HttpServletRequest req, HttpServletResponse res, org.springframework.security.access.AccessDeniedException e) throws IOException {
            res.setStatus(403);
            res.setContentType(MediaType.APPLICATION_JSON_VALUE);
            res.setCharacterEncoding(StandardCharsets.UTF_8.name());
            res.getWriter().write("{\"status\":403,\"error\":\"Forbidden\",\"message\":\"접근 권한이 없습니다.\",\"path\":\""+req.getRequestURI()+"\"}");
        }
    }
}
