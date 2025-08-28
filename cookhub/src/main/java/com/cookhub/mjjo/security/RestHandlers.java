package com.cookhub.mjjo.security;

import jakarta.servlet.http.*;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.*;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

//Spring Security 필터 체인 단계에서 발생하는 인증/인가 실패를 JSON 본문으로 표준화해 응답
public class RestHandlers {
	
	/* JsonAuthEntryPoint → 401 Unauthorized - 인증이 아예 없는 요청 : 기본값은 리다이렉트(폼 로그인)나 빈 본문일 수 있어 SPA/REST에서는 불편함 */
    public static class JsonAuthEntryPoint implements AuthenticationEntryPoint {
        @Override public void commence(HttpServletRequest req, HttpServletResponse res, AuthenticationException e) throws IOException {
            res.setStatus(401);
            res.setContentType(MediaType.APPLICATION_JSON_VALUE);
            res.setCharacterEncoding(StandardCharsets.UTF_8.name());
            res.getWriter().write("{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"인증이 필요합니다.\",\"path\":\""+req.getRequestURI()+"\"}");
        }
    }
    
	/* JsonAccessDeniedHandler → 403 Forbidden - 인증은 됐지만 권한이 부족한 요청 : 여기서 상태코드 + JSON 바디를 명확히 내려주면 프론트가 일관되게 처리 가능 */
    public static class JsonAccessDeniedHandler implements AccessDeniedHandler {
        @Override public void handle(HttpServletRequest req, HttpServletResponse res, org.springframework.security.access.AccessDeniedException e) throws IOException {
            res.setStatus(403);
            res.setContentType(MediaType.APPLICATION_JSON_VALUE);
            res.setCharacterEncoding(StandardCharsets.UTF_8.name());
            res.getWriter().write("{\"status\":403,\"error\":\"Forbidden\",\"message\":\"접근 권한이 없습니다.\",\"path\":\""+req.getRequestURI()+"\"}");
        }
    }
}
