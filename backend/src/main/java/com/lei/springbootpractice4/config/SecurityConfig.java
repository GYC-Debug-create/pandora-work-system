// 文件: backend/src/main/java/com/lei/springbootpractice4/config/SecurityConfig.java
package com.lei.springbootpractice4.config;

import org.springframework.beans.factory.annotation.Autowired; // 引入 Autowired
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter; // 引入 UsernamePasswordAuthenticationFilter

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired // 注入 JWT 过滤器
    private JwtRequestFilter jwtRequestFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll() // <--- 暂时允许所有请求，用于测试
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // 使用无状态 Session
                );

        // 允许 h2-console 在 iframe 中显示
        http.headers(headers -> headers.frameOptions(frameOptions -> frameOptions.sameOrigin()));

        // 在 UsernamePasswordAuthenticationFilter 之前添加 JWT 过滤器
        http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}