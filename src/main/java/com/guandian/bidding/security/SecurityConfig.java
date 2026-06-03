package com.guandian.bidding.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.guandian.bidding.common.api.R;
import com.guandian.bidding.common.api.ResultCode;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Spring Security 配置：无状态 JWT，放行登录/文档/游客接口，其余需鉴权。
 */
@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /** 放行路径（游客可访问 + 文档 + 登录注册）。 */
    private static final String[] WHITELIST = {
            "/api/ping",
            "/api/auth/register",
            "/api/auth/login",
            "/api/auth/sms-code",
            "/api/auth/wechat/login",
            "/api/home/announcements",
            "/api/home/winners",
            "/api/search",
            "/api/search/hot",
            "/api/tenders/*",
            "/api/winners/*",
            "/api/articles",
            "/api/articles/*",
            "/doc.html", "/webjars/**", "/v3/api-docs/**", "/swagger-ui/**", "/favicon.ico"
    };

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .antMatchers(WHITELIST).permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling(e -> e
                        .authenticationEntryPoint((req, resp, ex) -> write(resp, ResultCode.UNAUTHORIZED))
                        .accessDeniedHandler((req, resp, ex) -> write(resp, ResultCode.FORBIDDEN)))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    private void write(HttpServletResponse resp, ResultCode code) throws IOException {
        resp.setStatus(code == ResultCode.UNAUTHORIZED ? 401 : 403);
        resp.setContentType(MediaType.APPLICATION_JSON_VALUE);
        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resp.getWriter().write(new ObjectMapper().writeValueAsString(R.fail(code)));
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
