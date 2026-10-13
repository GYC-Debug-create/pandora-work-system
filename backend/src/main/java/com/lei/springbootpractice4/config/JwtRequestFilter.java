// 文件: backend/src/main/java/com/lei/springbootpractice4/config/JwtRequestFilter.java
package com.lei.springbootpractice4.config;

import com.lei.springbootpractice4.mapper.UserMapper;
import com.lei.springbootpractice4.pojo.UsersPojo;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * JWT 过滤器：只负责「识别身份」。
 * - token 有效：把用户和权限写进 SecurityContext（管理员额外带 ROLE_ADMIN）
 * - 无 token / token 无效 / 已过期 / 已退出(黑名单)：不写身份，并把原因放到 request 属性里，
 *   由 SecurityConfig 里的 401 入口统一返回。
 * 「哪些接口要登录、哪些要管理员」在 SecurityConfig 里配置。
 */
@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    /** 认证失败原因，供 SecurityConfig 的 401 响应读取 */
    public static final String AUTH_ERROR_ATTRIBUTE = "jwt.auth.error";

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private TokenBlacklist tokenBlacklist;

    @Autowired
    private UserMapper userMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String requestURI = request.getRequestURI();

        // 检查是否是登录接口
        if ("/api/user/login".equals(requestURI)) {
            // 检查当前请求是否安全（即是否通过 HTTPS 访问）
            if (!request.isSecure()) {
                // 如果是 HTTP 访问登录接口，直接拒绝，返回 403 或重定向
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setCharacterEncoding("UTF-8");
                response.setContentType("application/json");
                response.getWriter().write("{\"error\": \"为了您的数据机密性，登录接口必须使用 HTTPS 访问！\"}");
                return;
            }
        }

        String jwtToken = resolveToken(request);
        if (jwtToken != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            authenticate(jwtToken, request);
        }
        chain.doFilter(request, response);
    }

    /** 从 Authorization: Bearer xxx 中取出 token，没有则返回 null */
    public static String resolveToken(HttpServletRequest request) {
        final String requestTokenHeader = request.getHeader("Authorization");
        if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
            String token = requestTokenHeader.substring(7).trim();
            return token.isEmpty() ? null : token;
        }
        return null;
    }

    private void authenticate(String jwtToken, HttpServletRequest request) {
        // 1. 已退出登录的 token 直接视为无效
        if (tokenBlacklist.contains(jwtToken)) {
            request.setAttribute(AUTH_ERROR_ATTRIBUTE, "登录已退出，请重新登录");
            return;
        }

        // 2. 解析并校验签名、过期时间（解析失败会抛异常）
        Claims claims;
        try {
            claims = jwtUtil.getClaimFromToken(jwtToken, c -> c);
        } catch (ExpiredJwtException e) {
            request.setAttribute(AUTH_ERROR_ATTRIBUTE, "登录已过期，请重新登录");
            return;
        } catch (JwtException | IllegalArgumentException e) {
            request.setAttribute(AUTH_ERROR_ATTRIBUTE, "无效的登录凭证");
            return;
        }

        // 3. 以数据库中的用户为准：用户被删除则 token 失效；角色变更立即生效
        String userId = claims.get("userId", String.class);
        UsersPojo user = userId == null ? null : userMapper.selectById(userId);
        if (user == null) {
            request.setAttribute(AUTH_ERROR_ATTRIBUTE, "用户不存在或已被删除，请重新登录");
            return;
        }

        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(RoleConstants.AUTHORITY_USER));
        if (RoleConstants.isAdmin(user.getRoleId())) {
            authorities.add(new SimpleGrantedAuthority(RoleConstants.AUTHORITY_ADMIN));
        }

        UserDetails userDetails = new User(user.getUsername(), "", authorities);
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userDetails, null, authorities);
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
