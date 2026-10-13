// 文件: backend/src/main/java/com/lei/springbootpractice4/config/SecurityConfig.java
package com.lei.springbootpractice4.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired; // 引入 Autowired
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter; // 引入 UsernamePasswordAuthenticationFilter

import java.io.IOException;
import java.util.Map;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired // 注入 JWT 过滤器
    private JwtRequestFilter jwtRequestFilter;

    @Autowired
    private ObjectMapper objectMapper;

    /** 管理员角色（hasRole 会自动补 ROLE_ 前缀，对应 RoleConstants.AUTHORITY_ADMIN） */
    private static final String ADMIN = RoleConstants.ADMIN_ROLE_NAME;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // Spring 内部的错误转发（/error）不拦截，否则真实错误会被吞成 401
                        .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                        .requestMatchers("/error").permitAll()
                        // 匿名入口：仅登录接口
                        .requestMatchers(HttpMethod.POST, "/api/user/web-login", "/api/user/login").permitAll()
                        // 管理员写操作：创建/修改/删除用户、公司事项增删改
                        .requestMatchers(HttpMethod.POST, "/api/user/registerByAdmin").hasRole(ADMIN)
                        .requestMatchers(HttpMethod.PUT, "/api/users/**").hasRole(ADMIN)
                        .requestMatchers(HttpMethod.DELETE, "/api/users/**").hasRole(ADMIN)
                        .requestMatchers(HttpMethod.POST, "/api/company-matters", "/api/company-matters/**").hasRole(ADMIN)
                        .requestMatchers(HttpMethod.PUT, "/api/company-matters/**").hasRole(ADMIN)
                        .requestMatchers(HttpMethod.DELETE, "/api/company-matters/**").hasRole(ADMIN)
                        // 其余接口一律需要有效 token
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // 使用无状态 Session
                )
                .exceptionHandling(ex -> ex
                        // 未登录 / token 无效 / 已退出 → 401
                        .authenticationEntryPoint((request, response, authException) -> {
                            Object reason = request.getAttribute(JwtRequestFilter.AUTH_ERROR_ATTRIBUTE);
                            writeJson(response, HttpServletResponse.SC_UNAUTHORIZED,
                                    reason != null ? reason.toString() : "未登录或登录已失效，请先登录");
                        })
                        // 已登录但权限不足 → 403
                        .accessDeniedHandler((request, response, accessDeniedException) ->
                                writeJson(response, HttpServletResponse.SC_FORBIDDEN, "权限不足，仅管理员可执行此操作"))
                );

        // 允许 h2-console 在 iframe 中显示
        http.headers(headers -> headers.frameOptions(frameOptions -> frameOptions.sameOrigin()));

        // 在 UsernamePasswordAuthenticationFilter 之前添加 JWT 过滤器
        http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * JwtRequestFilter 是 @Component，Spring Boot 默认还会把它注册成普通 Servlet 过滤器。
     * 这里关掉那份注册，让它只在 Security 过滤链里执行一次。
     */
    @Bean
    public FilterRegistrationBean<JwtRequestFilter> jwtRequestFilterRegistration(JwtRequestFilter filter) {
        FilterRegistrationBean<JwtRequestFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }

    private void writeJson(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setCharacterEncoding("UTF-8");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(Map.of("error", message)));
    }
}
