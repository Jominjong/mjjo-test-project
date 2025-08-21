package com.cookhub.mjjo.config;

import com.cookhub.mjjo.security.JwtAuthenticationFilter;
import com.cookhub.mjjo.security.RestHandlers.JsonAccessDeniedHandler;
import com.cookhub.mjjo.security.RestHandlers.JsonAuthEntryPoint;
import com.cookhub.mjjo.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandlerImpl;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@EnableMethodSecurity(prePostEnabled = true)
@EnableWebSecurity(debug = true)
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtUtil jwtUtil;

    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(Customizer.withDefaults())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll()
            )
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                .accessDeniedHandler(new AccessDeniedHandlerImpl())
            )
            .formLogin(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable)
            .logout(AbstractHttpConfigurer::disable);

        return http.build();
    }
    
    
//    // 1) Swagger / 문서 전용 체인: 무조건 오픈
//    @Bean
//    @Order(1)
//    public SecurityFilterChain swaggerChain(HttpSecurity http) throws Exception {
//        http
//            .securityMatcher(
//                "/v3/api-docs",            // 기본 JSON
//                "/v3/api-docs/**",         // 그룹/스펙
//                "/swagger-ui.html",
//                "/swagger-ui/**",
//                "/swagger-resources/**",
//                "/webjars/**"
//            )
//            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
//            .csrf(csrf -> csrf.disable())
//            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
//        // ⚠️ 여긴 JWT 필터 넣지 않음
//        return http.build();
//    }
//
//    // 2) 나머지 API 체인: JWT 필요
//    @Bean
//    @Order(2)
//    public SecurityFilterChain apiChain(HttpSecurity http) throws Exception {
//        var jwtFilter = new JwtAuthenticationFilter(jwtUtil);
//
//        http
//            .csrf(csrf -> csrf.disable())
//            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
//            .exceptionHandling(ex -> ex
//                .authenticationEntryPoint(new JsonAuthEntryPoint())
//                .accessDeniedHandler(new JsonAccessDeniedHandler())
//            )
//            .authorizeHttpRequests(auth -> auth
//                .requestMatchers(
//                    "/api/register/**",
//                    "/api/login",
//                    "/api/auth/refresh"
//                ).permitAll()
//                .anyRequest().authenticated()
//            )
//            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
//            .httpBasic(Customizer.withDefaults())
//            .formLogin(form -> form.disable());
//
//        return http.build();
//    }
//    
//    @Bean
//    public WebSecurityCustomizer webSecurityCustomizer() {
//        return web -> web.ignoring().requestMatchers(
//            "/v3/api-docs", "/v3/api-docs/**",
//            "/swagger-ui.html", "/swagger-ui/**",
//            "/swagger-resources/**", "/webjars/**"
//        );
//    }

}
