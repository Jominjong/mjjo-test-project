package com.cookhub.mjjo.security;

import java.util.List;

/* 인증이 끝난 후 애플리케이션이 다룰 내부 사용자 정보를 담는 불변 DTO */
public record AuthUser(Integer userNo, String email, List<String> roles) {}
