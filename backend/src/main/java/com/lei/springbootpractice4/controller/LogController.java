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
import org.springframework.web.bind.annotation.*;

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


    /**
     * 修改日志描述
     * 权限：能看到日志的人就能改
     */
    @PutMapping("/{logId}/description")
    public ResponseEntity<Object> updateLogDescription(
            @PathVariable Integer logId,
            @RequestBody Map<String, String> payload, // 接收 {"description": "..."}
            HttpServletRequest request) {

        String newDescription = payload.get("description");
        if (newDescription == null) { // 允许为空字符串，但不允许 null
            return ResponseEntity.badRequest().body(Map.of("error", "请求体必须包含 description 字段"));
        }

        try {
            // 1. 获取当前用户
            UsersPojo currentUser = getCurrentUser(request);
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "无效的用户凭证"));
            }

            // 2. 调用 Service
            LogPojo updatedLog = logService.updateLogDescription(logId, newDescription, currentUser);
            return ResponseEntity.ok(updatedLog);

        } catch (IllegalArgumentException e) { // 日志不存在
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (SecurityException e) { // 无权限
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            System.err.println("更新日志描述失败: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "更新日志描述失败"));
        }
    }
}