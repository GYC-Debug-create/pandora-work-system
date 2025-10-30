package com.lei.springbootpractice4.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page; // 引入 Page
import com.lei.springbootpractice4.config.JwtUtil;
import com.lei.springbootpractice4.mapper.UserMapper; // <--- 引入 UserMapper
import com.lei.springbootpractice4.pojo.TaskPojo;
import com.lei.springbootpractice4.pojo.UsersPojo; // <--- 引入 UsersPojo
import com.lei.springbootpractice4.service.TaskService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    @Autowired
    private TaskService taskService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserMapper userMapper; // <--- 注入 UserMapper

    // --- createTask 和 addMembers 方法保持不变 ---
    @PostMapping
    public ResponseEntity<Object> createTask(@RequestBody TaskPojo task, HttpServletRequest request) {
        try {
            String creatorId = getUserIdFromRequest(request);
            if (creatorId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "无法从Token获取用户信息"));
            }
            TaskPojo createdTask = taskService.createTask(task, creatorId);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdTask);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            System.err.println("创建主任务时发生内部错误: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "创建主任务失败"));
        }
    }

    @PostMapping("/{taskId}/members")
    public ResponseEntity<Object> addMembers(
            @PathVariable Integer taskId,
            @RequestBody Map<String, List<String>> payload,
            HttpServletRequest request) {
        List<String> userIds = payload.get("userIds");
        if (userIds == null || userIds.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "请求体必须包含 'userIds' 列表且不能为空"));
        }
        try {
            String currentUserId = getUserIdFromRequest(request);
            if (currentUserId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "无法从Token获取用户信息"));
            }
            taskService.addMembersToTask(taskId, userIds, currentUserId);
            Map<String, Object> response = Map.of(
                    "message", "成员添加成功", "taskId", taskId, "newStatus", "IN_PROGRESS"
            );
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            System.err.println("添加任务成员时发生内部错误: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "添加成员失败"));
        }
    }

    // --- 新增：获取任务列表 (带权限) ---
    @GetMapping
    public ResponseEntity<Object> getTaskList(
            @RequestParam Map<String, Object> params,
            HttpServletRequest request) {
        try {
            // 获取当前登录用户的完整信息 (用于权限判断)
            UsersPojo currentUser = getCurrentUser(request);
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "无效的用户凭证"));
            }

            Page<TaskPojo> taskPage = taskService.getTaskList(params, currentUser);
            return ResponseEntity.ok(taskPage);
        } catch (Exception e) {
            System.err.println("获取任务列表失败: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "获取任务列表失败"));
        }
    }

    // --- 新增：获取任务详情 (带权限) ---
    @GetMapping("/{taskId}")
    public ResponseEntity<Object> getTaskById(
            @PathVariable Integer taskId,
            HttpServletRequest request) {
        try {
            UsersPojo currentUser = getCurrentUser(request);
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "无效的用户凭证"));
            }

            TaskPojo task = taskService.getTaskById(taskId, currentUser);
            return ResponseEntity.ok(task);

        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            System.err.println("获取任务详情失败: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "获取任务详情失败"));
        }
    }

    // --- 新增：更新任务信息 (带权限) ---
    @PutMapping("/{taskId}")
    public ResponseEntity<Object> updateTask(
            @PathVariable Integer taskId,
            @RequestBody TaskPojo taskUpdateData, // 只包含要更新的字段
            HttpServletRequest request) {
        try {
            UsersPojo currentUser = getCurrentUser(request);
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "无效的用户凭证"));
            }

            TaskPojo updatedTask = taskService.updateTask(taskId, taskUpdateData, currentUser);
            return ResponseEntity.ok(updatedTask);

        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            System.err.println("更新任务失败: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "更新任务失败"));
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
            // 从数据库获取用户的完整信息，包括 roleId 和 departmentId
            return userMapper.selectById(userId);
        }
        return null;
    }
}