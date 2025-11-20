package com.lei.springbootpractice4.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page; // 引入 Page
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.lei.springbootpractice4.config.JwtUtil;
import com.lei.springbootpractice4.mapper.UserMapper; // <--- 引入 UserMapper
import com.lei.springbootpractice4.pojo.TaskPojo;
import com.lei.springbootpractice4.pojo.UsersPojo; // <--- 引入 UsersPojo
import com.lei.springbootpractice4.service.TaskService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    @Autowired
    private TaskService taskService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserMapper userMapper; // <--- 注入 UserMapper

    @Value("${file.upload-dir}")
    private String uploadDirConfig;


    private final ObjectMapper objectMapper;

    // 2. 在构造函数中初始化并配置
    public TaskController() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule()); // <--- 关键修复：注册 Java 8 时间模块
    }


    /**
     * 从请求头中提取地理位置信息
     */
    private String getLocationFromRequest(HttpServletRequest request) {
        String locationHeader = request.getHeader("X-Device-Location");

        if (locationHeader == null || locationHeader.isBlank()) {
            return null;
        }

        // 如果前端发送的是"未知位置"，我们存为 null
        if ("未知位置".equals(locationHeader)) {
            return null;
        }

        try {
            // 关键：对从 Header 中获取的、可能被 URL 编码的字符串进行解码
            return URLDecoder.decode(locationHeader, StandardCharsets.UTF_8.toString());
        } catch (Exception e) {
            // 如果解码失败（例如，它已经是未编码的普通 ASCII 字符串），
            // 则打印一个警告，并尝试使用原始值
            System.err.println("警告: URL 解码 X-Device-Location 失败。使用原始值: " + locationHeader);
            return locationHeader;
        }
    }



    // --- createTask 和 addMembers 方法保持不变 ---
    /*@PostMapping
    public ResponseEntity<Object> createTask(@RequestBody TaskPojo task, HttpServletRequest request) {
        try {
            String creatorId = getUserIdFromRequest(request);
            String location = getLocationFromRequest(request);
            if (creatorId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "无法从Token获取用户信息"));
            }
            TaskPojo createdTask = taskService.createTask(task, creatorId,location);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdTask);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            System.err.println("创建主任务时发生内部错误: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "创建主任务失败"));
        }
    }*/
    @PostMapping("/with-attachment") // <--- 新的 API 路径，用于支持带附件的创建
    public ResponseEntity<Object> createTaskWithAttachment(
            @RequestParam(value = "files", required = false) List<MultipartFile> files, // 文件列表
            @RequestParam("taskData") String taskDataJson,                           // 任务数据 JSON 字符串
            HttpServletRequest request) {

        try {
            // 1. 获取用户和位置信息
            String creatorId = getUserIdFromRequest(request);
            String location = getLocationFromRequest(request);
            if (creatorId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "无法从Token获取用户信息"));
            }

            // 2. 解析任务数据 JSON 字符串为 POJO
            TaskPojo task = objectMapper.readValue(taskDataJson, TaskPojo.class);
            if (task.getTaskName() == null || task.getResponsiblePersonId() == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "任务名称和负责人ID不能为空"));
            }

            // 3. 处理文件上传（文件保存到本地，并获取 JSON 字符串）
            String attachmentJson = handleFileUpload(files);

            // 4. 调用 Service，传递附件 JSON
            TaskPojo createdTask = taskService.createTask(task, creatorId, location, attachmentJson);

            return ResponseEntity.status(HttpStatus.CREATED).body(createdTask);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            System.err.println("创建带附件主任务时发生内部错误: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "创建带附件主任务失败"));
        }
    }

    private String handleFileUpload(List<MultipartFile> files) throws IOException, IOException {
        if (files == null || files.isEmpty()) {
            return null;
        }

        File configuredUploadDir = new File(uploadDirConfig);
        Path absoluteUploadPath = Paths.get(configuredUploadDir.getAbsolutePath());

        if (!Files.exists(absoluteUploadPath)) {
            Files.createDirectories(absoluteUploadPath);
        }

        List<Map<String, String>> fileDetails = new java.util.ArrayList<>();

        for (MultipartFile file : files) {
            String originalFilename = file.getOriginalFilename();
            String fileExtension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String uniqueFileName = UUID.randomUUID().toString() + fileExtension;

            Path targetFilePath = absoluteUploadPath.resolve(uniqueFileName);
            File destFile = targetFilePath.toFile();

            file.transferTo(destFile);

            fileDetails.add(Map.of(
                    "uniqueFileName", uniqueFileName,
                    "originalFileName", originalFilename,
                    "fileType", file.getContentType()
            ));
        }

        return objectMapper.writeValueAsString(fileDetails);
    }

    @PostMapping("/{taskId}/members")
    public ResponseEntity<Object> addMembers(
            @PathVariable Integer taskId,
            @RequestBody Map<String, List<String>> payload,
            HttpServletRequest request) {
        List<String> userIds = payload.get("userIds");
        String location = getLocationFromRequest(request);
        if (userIds == null || userIds.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "请求体必须包含 'userIds' 列表且不能为空"));
        }
        try {
            String currentUserId = getUserIdFromRequest(request);
            if (currentUserId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "无法从Token获取用户信息"));
            }
            taskService.addMembersToTask(taskId, userIds, currentUserId,location);
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
            String location = getLocationFromRequest(request);
            UsersPojo currentUser = getCurrentUser(request);
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "无效的用户凭证"));
            }

            TaskPojo updatedTask = taskService.updateTask(taskId, taskUpdateData, currentUser,location);
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