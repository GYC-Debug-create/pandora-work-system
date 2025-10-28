// 文件: backend/src/main/java/com/lei/springbootpractice4/config/JwtRequestFilter.java
package com.lei.springbootpractice4.config;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList; // 引入 ArrayList
import java.util.Collections;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        // ... (获取 token 和 username 的逻辑保持不变) ...
        final String requestTokenHeader = request.getHeader("Authorization");
        String username = null;
        String jwtToken = null;
        if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
            jwtToken = requestTokenHeader.substring(7);
            try {
                username = jwtUtil.getUsernameFromToken(jwtToken);
            } catch (IllegalArgumentException e) {
                System.out.println("Unable to get JWT Token");
            } catch (ExpiredJwtException e) {
                System.out.println("JWT Token has expired");
            } catch (io.jsonwebtoken.MalformedJwtException e) { // <--- 捕获之前的 MalformedJwtException
                System.out.println("Invalid JWT Token format");
            }
        } else {
            logger.warn("JWT Token does not begin with Bearer String or is missing"); // 修改日志信息
        }


        // 验证 token 并设置 Security Context
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // --- 修改这里：为用户赋予一个默认权限 ---
            // 实际项目中，这里应该从数据库加载用户的真实权限
            UserDetails userDetails = new User(username, "",
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")) // <--- 赋予 "ROLE_USER" 权限
            );

            // 验证 token
            if (jwtUtil.validateToken(jwtToken, userDetails)) {
                UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities()); // 使用 userDetails 中的权限
                usernamePasswordAuthenticationToken
                        .setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                // 设置到 SecurityContext
                SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
            }
        }
        chain.doFilter(request, response);
    }
}