package com.cookhub.mjjo.config;

import com.cookhub.mjjo.security.JwtAuthenticationFilter;
import com.cookhub.mjjo.security.RestHandlers.JsonAccessDeniedHandler;
import com.cookhub.mjjo.security.RestHandlers.JsonAuthEntryPoint;
import com.cookhub.mjjo.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity(debug = false)
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtUtil jwtUtil;

    //Swagger
    @Bean
    @Order(0)
    public SecurityFilterChain swaggerChain(HttpSecurity http) throws Exception {
        return http
            .securityMatcher(
                "/v3/api-docs", "/v3/api-docs/**",
                "/swagger-ui.html", "/swagger-ui/**",
                "/swagger-resources/**", "/webjars/**"
            )
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
            .build();
    }

    //JWT 필요한 나머지 API 
    @Bean
    @Order(1)
    public SecurityFilterChain apiChain(HttpSecurity http) throws Exception {
        var jwtFilter = new JwtAuthenticationFilter(jwtUtil);

        return http
            //API와 에러만 이 체인이 처리
            .securityMatcher("/recipes", "/recipes/**", "/auth/**", "/error")
            .csrf(AbstractHttpConfigurer::disable)
            .cors(Customizer.withDefaults())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(new JsonAuthEntryPoint())
                .accessDeniedHandler(new JsonAccessDeniedHandler())
            )
            .authorizeHttpRequests(a -> a
                //반드시 허용
                .requestMatchers("/error").permitAll()
                //CORS preflight 허용 (필요 시)
                .requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll()

                //공개 API
                .requestMatchers(
                		//legacy
                		"/api/register/**", "/api/login",
                		"/api/auth/refresh", "/api/password/**",
                		
                		//final
                		"/auth/login", "/auth/refresh",
                		"/auth/register", "/auth/password/**",
                		"/auth/register/check", "/auth/register/verify"
                ).permitAll()

                //그 외는 인증 필요
                .requestMatchers(
                		//legacy
                		"/api/recipes/**",
                		
                		//new
                		"/recipes/**", "/recipes"
                ).authenticated()
                
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
            .formLogin(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable)
            .logout(AbstractHttpConfigurer::disable)
            .build();
    }
    

   // CORS 정의
   @Bean
   public org.springframework.web.cors.CorsConfigurationSource corsConfigurationSource() {
       var config = new org.springframework.web.cors.CorsConfiguration();
       // 프론트 개발 서버(Vite)
       config.setAllowedOrigins(java.util.List.of("http://localhost:5173"));
       // 배포 도메인도 있다면 나중에 여기에 추가
       // config.setAllowedOrigins(List.of("http://localhost:5173", "https://cookhub.your-domain.com"));

       config.setAllowedMethods(java.util.List.of("GET","POST","PUT","DELETE","OPTIONS"));
       // 커스텀 요청 헤더 포함
       config.setAllowedHeaders(java.util.List.of("Content-Type","Authorization","X-Signup-Token"));
       // 쿠키/자격증명 사용 시 true (쿠키 기반 RT를 쓴다면 켜세요)
       config.setAllowCredentials(true);
       // 프리플라이트 캐시
       config.setMaxAge(3600L);

       var source = new org.springframework.web.cors.UrlBasedCorsConfigurationSource();
       source.registerCorsConfiguration("/**", config);
       return source;
   }
    
    //비밀번호 Encoder 선언
    @Bean
    public BCryptPasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }
    
    
    //직접 빈을 정의해서 기본 유저 생성 제한
    @Bean
    public org.springframework.security.core.userdetails.UserDetailsService userDetailsService() {
        return new org.springframework.security.provisioning.InMemoryUserDetailsManager(); //비어있는 사용자 저장소
    }
}

