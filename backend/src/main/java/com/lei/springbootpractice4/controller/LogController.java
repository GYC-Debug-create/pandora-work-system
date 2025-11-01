package com.lei.springbootpractice4.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lei.springbootpractice4.config.JwtUtil;
import com.lei.springbootpractice4.mapper.UserMapper;
import com.lei.springbootpractice4.pojo.LogPojo;
import com.lei.springbootpractice4.pojo.UsersPojo;
import com.lei.springbootpractice4.service.LogService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/logs") // 对应 API 文档的 /logs
public class LogController {

    @Autowired
    private LogService logService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserMapper userMapper; // 用于获取当前用户

    @GetMapping
    public ResponseEntity<Object> getLogs(
            @RequestParam Map<String, Object> params,
            HttpServletRequest request) {

        try {
            // 1. 获取当前登录用户的完整信息 (用于权限判断)
            UsersPojo currentUser = getCurrentUser(request);
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "无效的用户凭证"));
            }

            // 2. 调用 Service 获取日志
            Page<LogPojo> logPage = logService.getLogs(params, currentUser);

            // 3. 返回分页结果 (MyBatis Plus Page 对象已包含 records, total, size, current 等)
            return ResponseEntity.ok(logPage);

        } catch (Exception e) {
            System.err.println("获取日志列表失败: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "获取日志列表失败"));
        }
    }

    // --- 辅助方法：从 Token 获取 UserId ---
    private String getUserIdFromRequest(HttpServletRequest request) {
        final String requestTokenHeader = request.getHeader("Authorization");
        String currentUserId = null;
        if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
            String jwtToken = requestTokenHeader.substring(7);
            try {
                currentUserId = jwtUtil.getUserIdFromToken(jwtToken);
            } catch (Exception e) {
                System.err.println("从Token获取userId失败: " + e.getMessage());
            }
        }
        return currentUserId;
    }

    // --- 辅助方法：获取当前用户的完整 Pojo ---
    private UsersPojo getCurrentUser(HttpServletRequest request) {
        String userId = getUserIdFromRequest(request);
        if (userId != null) {
            return userMapper.selectById(userId);
        }
        return null;
    }
}