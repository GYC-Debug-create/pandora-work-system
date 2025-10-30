package com.lei.springbootpractice4.controller;

import com.lei.springbootpractice4.config.JwtUtil;
import com.lei.springbootpractice4.pojo.SubTaskPojo;
import com.lei.springbootpractice4.service.SubTaskService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication; // 用于获取当前用户信息
import org.springframework.security.core.context.SecurityContextHolder; // 用于获取当前用户信息
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/subtasks") // 基础路径 /api/subtasks
public class SubTaskController {

    @Autowired
    private SubTaskService subTaskService;

    @Autowired // 注入 JwtUtil
    private JwtUtil jwtUtil;

    @PostMapping("/{subTaskId}/complete")
    public ResponseEntity<Object> completeSubTask(
            @PathVariable Integer subTaskId,
            HttpServletRequest request) {

        try {
            // 1. 从 Token 获取当前用户 ID
            String currentUserId = getUserIdFromRequest(request);
            if (currentUserId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "无法从Token获取用户信息"));
            }

            // 2. 调用 Service 将任务标记为完成
            SubTaskPojo completedSubTask = subTaskService.completeSubTask(subTaskId, currentUserId);
            return ResponseEntity.ok(completedSubTask);

        } catch (IllegalArgumentException e) { // 子任务不存在
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (SecurityException e) { // 无权限 (不是负责人)
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) { // 状态不符 (非个人任务 或 已完成)
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            System.err.println("标记任务完成时发生内部错误: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "操作失败"));
        }
    }

    @PostMapping("/{subTaskId}/review")
    public ResponseEntity<Object> reviewSubTask(
            @PathVariable Integer subTaskId,
            @RequestBody Map<String, Object> payload, // 接收 { "approved": true/false, "comment": "..." }
            HttpServletRequest request) {

        // 1. 从 payload 中获取审批结果和评论
        Boolean approved = (Boolean) payload.get("approved");
        String comment = (String) payload.get("comment");

        // 基本校验
        if (approved == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "请求体必须包含 'approved' 字段 (true 或 false)"));
        }
        if (!approved && (comment == null || comment.isBlank())) {
            return ResponseEntity.badRequest().body(Map.of("error", "驳回时必须提供 'comment' 字段"));
        }

        try {
            // 2. 从 Token 获取审批人 ID
            String reviewerId = getUserIdFromRequest(request);
            if (reviewerId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "无法从Token获取用户信息"));
            }

            // 3. 调用 Service 进行审批
            SubTaskPojo reviewedSubTask = subTaskService.reviewSubTask(subTaskId, approved, comment, reviewerId);
            return ResponseEntity.ok(reviewedSubTask);

        } catch (IllegalArgumentException e) { // 子任务不存在 或 驳回无评论
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        } catch (SecurityException e) { // 无权限审批
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) { // 状态不符 (不是 PENDING_APPROVAL)
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            System.err.println("审批子任务时发生内部错误: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "审批子任务失败"));
        }
    }
    // --- 新增结束 ---




    // --- 创建子任务接口 (POST /api/subtasks) ---
    // 我们先实现这个接口，以便能创建子任务用于后续测试
    @PostMapping
    public ResponseEntity<Object> createSubTask(@RequestBody SubTaskPojo subTask, HttpServletRequest request) { // 注入 request
        try {
            // --- 从 Token 获取 userId ---
            String currentUserId = getUserIdFromRequest(request);
            if (currentUserId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "无法从Token获取用户信息"));
            }
            // --- 获取结束 ---

            SubTaskPojo createdSubTask = subTaskService.createSubTask(subTask, currentUserId); // 传递 userId
            return ResponseEntity.status(HttpStatus.CREATED).body(createdSubTask);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            System.err.println("创建子任务时发生内部错误: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "创建子任务失败"));
        }
    }


    // --- 提交交付物接口 (PUT /api/subtasks/{subTaskId}/submit) ---
    @PutMapping("/{subTaskId}/submit")
    public ResponseEntity<Object> submitDeliverable(
            @PathVariable Integer subTaskId,
            @RequestBody Map<String, String> payload, HttpServletRequest request) { // 注入 request

        String deliverableContentJson = payload.get("deliverableContent");
        if (deliverableContentJson == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "请求体必须包含 deliverableContent 字段"));
        }

        try {
            // --- 从 Token 获取 userId ---
            String currentUserId = getUserIdFromRequest(request);
            if (currentUserId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "无法从Token获取用户信息"));
            }
            // --- 获取结束 ---

            SubTaskPojo updatedSubTask = subTaskService.submitDeliverable(subTaskId, deliverableContentJson, currentUserId); // 传递 userId
            return ResponseEntity.ok(updatedSubTask);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            System.err.println("提交交付物时发生内部错误: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "提交交付物失败"));
        }
    }
    // --- 辅助方法：从 HttpServletRequest 中提取 userId ---
    private String getUserIdFromRequest(HttpServletRequest request) {
        final String requestTokenHeader = request.getHeader("Authorization");
        String currentUserId = null;
        if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
            String jwtToken = requestTokenHeader.substring(7);
            try {
                // 假设 token 验证已在 JwtRequestFilter 完成，这里直接提取 userId
                currentUserId = jwtUtil.getUserIdFromToken(jwtToken); // 使用新方法获取 userId
            } catch (Exception e) {
                System.err.println("从Token获取userId失败: " + e.getMessage());
                // 不向上抛出异常，让 Controller 返回 401 或 null
            }
        }

        return currentUserId;
    }

    // --- 后续可以添加审批接口 POST /api/subtasks/{subTaskId}/review ---
    // @PostMapping("/{subTaskId}/review")
    // public ResponseEntity<Object> reviewSubTask(...) { ... }

    // --- 后续可以添加获取子任务详情接口 GET /api/subtasks/{subTaskId} ---
    // @GetMapping("/{subTaskId}")
    // public ResponseEntity<SubTaskPojo> getSubTaskById(...) { ... }
}