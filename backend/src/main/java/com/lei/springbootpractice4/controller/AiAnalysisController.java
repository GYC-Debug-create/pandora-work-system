package com.lei.springbootpractice4.controller;

import com.lei.springbootpractice4.config.JwtUtil;
import com.lei.springbootpractice4.mapper.UserMapper;
import com.lei.springbootpractice4.pojo.UsersPojo;
import com.lei.springbootpractice4.service.AiAnalysisService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ai") // AI 相关接口的基础路径
public class AiAnalysisController {

    @Autowired
    private AiAnalysisService aiAnalysisService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserMapper userMapper;

    /**
     * 获取指定用户的 AI 分析数据
     */
    @GetMapping("/user-data/{userId}")
    public ResponseEntity<Object> getUserAiData(
            @PathVariable String userId,
            HttpServletRequest request) {

        try {
            // 1. 获取当前登录用户的完整信息 (用于权限判断)
            UsersPojo currentUser = getCurrentUser(request);
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "无效的用户凭证"));
            }

            // 2. 调用 Service 获取数据 (Service 内部会处理权限)
            Map<String, Object> aiData = aiAnalysisService.getAiAnalysisData(userId, currentUser);
            return ResponseEntity.ok(aiData);

        } catch (IllegalArgumentException e) { // 用户不存在
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (SecurityException e) { // 无权限
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            System.err.println("获取 AI 分析数据失败: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "获取数据失败"));
        }
    }

    // --- 辅助方法 (从 Token 获取用户 Pojo) ---

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

    private UsersPojo getCurrentUser(HttpServletRequest request) {
        String userId = getUserIdFromRequest(request);
        if (userId != null) {
            return userMapper.selectById(userId);
        }
        return null;
    }
}