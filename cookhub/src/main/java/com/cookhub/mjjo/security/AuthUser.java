package com.cookhub.mjjo.security;

import java.util.List;

public record AuthUser(Integer userNo, String email, List<String> roles) {}
