package com.lei.springbootpractice4.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page; // 引入Page
import com.lei.springbootpractice4.config.JwtUtil;
import com.lei.springbootpractice4.mapper.RoleMapper;
import com.lei.springbootpractice4.mapper.UserMapper;
import com.lei.springbootpractice4.pojo.RolePojo;
import com.lei.springbootpractice4.pojo.UsersPojo;
import com.lei.springbootpractice4.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map; // 引入Map

@RestController
// @RequestMapping("/user") // 保持/user前缀用于登录和注册
public class UserController { // 移除类级别的 RequestMapping 或修改

    @Autowired
    private UserService userService;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private RoleMapper roleMapper; // <--- 注入 RoleMapper
    @Autowired
    private UserMapper userMapper; // <--- 注入 UserMapper (用于辅助方法)

    // POST /user/registerByAdmin (保持不变)
    @PostMapping("/api/user/registerByAdmin")
    public ResponseEntity<String> registerByAdmin(@RequestBody UsersPojo user) {
        try {
            String rawPassword = user.getPasswordHash();
            user.setPasswordHash(rawPassword);
            boolean registSuccess = userService.registerByAdmin(user);
            if (registSuccess) {
                return ResponseEntity.ok("用户创建成功");
            } else {
                return ResponseEntity.badRequest().body("创建失败, 用户名已存在");
            }
        } catch (org.springframework.dao.DuplicateKeyException e) {
            // 处理邮箱或其他唯一性约束冲突
            if (e.getMessage().contains("EMAIL")) {
                return ResponseEntity.badRequest().body("创建失败, 该邮箱已被使用");
            } else {
                return ResponseEntity.badRequest().body("创建失败, 数据重复");
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body("创建失败, 系统错误: " + e.getMessage());
        }
    }

    // POST /user/login (保持不变)
    // 文件: backend/src/main/java/com/lei/springbootpractice4/controller/UserController.java
    @PostMapping("/api/user/login")
    public ResponseEntity<Object> login(@RequestBody UsersPojo user) {
        UsersPojo loginUser = userService.login(user.getUsername(), user.getPasswordHash());
        if (loginUser != null) {
            loginUser.setPasswordHash(null);


            // --- 修改这里 ---
            // 创建 UserDetails，仍然使用 username 作为 principal
            final UserDetails userDetails = new User(loginUser.getUsername(), "", new ArrayList<>());
            // 创建 Claims，包含 userId
            Map<String, Object> claims = new HashMap<>();
            claims.put("userId", loginUser.getUserId()); // <--- 添加 userId 到 Claims
            // 使用包含 Claims 的方法生成 Token (需要修改 JwtUtil)
            if (loginUser.getRoleId() != null) {
                RolePojo role = roleMapper.selectById(loginUser.getRoleId());
                if (role != null) {
                    // 将角色名放入 claims，键名为 "role"
                    claims.put("role", role.getRoleName());
                }
            }
            final String token = jwtUtil.generateToken(claims, userDetails.getUsername());
            // --- 修改结束 ---

            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("user", loginUser);
            System.out.println("登录成功，准备返回 Token 和 User: " + response);
            return ResponseEntity.ok(response);
        } else {
            System.out.println("登录失败 (UserService 返回 null)，准备返回 401。");
            return ResponseEntity.status(401).body("登录失败,用户名或密码错误");
        }
    }

    // POST /api/user/web-login (Web端专用登录，仅限ADMIN)
    @PostMapping("/api/user/web-login")
    public ResponseEntity<Object> webLogin(@RequestBody UsersPojo user) {
        UsersPojo loginUser = userService.login(user.getUsername(), user.getPasswordHash());
        if (loginUser != null) {
            // Web端仅允许ADMIN登录
            if (loginUser.getRoleId() == null || loginUser.getRoleId() != 1) {
                return ResponseEntity.status(403).body("Web端仅限管理员登录");
            }

            loginUser.setPasswordHash(null);
            final UserDetails userDetails = new User(loginUser.getUsername(), "", new ArrayList<>());
            Map<String, Object> claims = new HashMap<>();
            claims.put("userId", loginUser.getUserId());
            RolePojo role = roleMapper.selectById(loginUser.getRoleId());
            if (role != null) {
                claims.put("role", role.getRoleName());
            }
            final String token = jwtUtil.generateToken(claims, userDetails.getUsername());

            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("user", loginUser);
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(401).body("登录失败,用户名或密码错误");
        }
    }

    // --- 新增API端点 ---

    /**
     * 获取用户列表 (支持分页和查询)
     * 前端请求示例: GET /api/users?current=1&size=10&username=张三&roleName=管理员
     */
    @GetMapping("/api/users")
    public ResponseEntity<Page<UsersPojo>> getUserList(
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String roleName) {
        
        System.out.println("Received params - current: " + current + ", size: " + size + ", username: " + username + ", roleName: " + roleName);
        
        // 将参数封装到Map中，保持与现有Service方法的兼容性
        Map<String, Object> params = new HashMap<>();
        params.put("page", current);  // 将 current 映射为 page
        params.put("size", size);
        if (username != null && !username.trim().isEmpty()) {
            params.put("username", username.trim());
        }
        if (roleName != null && !roleName.trim().isEmpty()) {
            params.put("roleName", roleName.trim());
        }
        
        Page<UsersPojo> userPage = userService.getUserList(params);
        return ResponseEntity.ok(userPage);
    }

    /**
     * 更新用户信息
     * 前端请求示例: PUT /users/uuid-of-user
     */
    @PutMapping("/api/users/{id}")
    public ResponseEntity<String> updateUser(@PathVariable String id, @RequestBody UsersPojo user) {
        boolean success = userService.updateUser(id, user);
        if (success) {
            return ResponseEntity.ok("用户信息更新成功");
        } else {
            // 可以更细化错误，例如返回404 Not Found
            return ResponseEntity.badRequest().body("更新失败，用户可能不存在或不允许修改");
        }
    }

    /**
     * 删除用户
     * 前端请求示例: DELETE /users/uuid-of-user
     */
    @DeleteMapping("/api/users/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable String id) {
        boolean success = userService.deleteUser(id);
        if (success) {
            return ResponseEntity.ok("用户删除成功");
        } else {
            // 可以更细化错误，例如返回404 Not Found 或 403 Forbidden
            return ResponseEntity.badRequest().body("删除失败，用户可能不存在或不允许删除");
        }
    }

    @PostMapping("/api/user/change-password")
    public ResponseEntity<Object> changePassword(
            @RequestBody Map<String, String> payload,
            HttpServletRequest request) {

        String oldPassword = payload.get("oldPassword");
        String newPassword = payload.get("newPassword");
        if (oldPassword == null || newPassword == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "请求体必须包含 oldPassword 和 newPassword"));
        }

        try {
            String currentUserId = getUserIdFromRequest(request);
            if (currentUserId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "无效的用户凭证"));
            }

            boolean success = userService.changePassword(currentUserId, oldPassword, newPassword);
            if (success) {
                return ResponseEntity.ok(Map.of("message", "密码修改成功"));
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "密码修改失败"));
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "服务器内部错误"));
        }
    }

    /**
     * 修改当前用户 MBTI
     */
    @PutMapping("/api/user/mbti")
    public ResponseEntity<Object> changeMbti(
            @RequestBody Map<String, String> payload,
            HttpServletRequest request) {

        String newMbti = payload.get("mbti");
        try {
            String currentUserId = getUserIdFromRequest(request);
            if (currentUserId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "无效的用户凭证"));
            }

            boolean success = userService.changeMbti(currentUserId, newMbti);
            if (success) {
                return ResponseEntity.ok(Map.of("message", "MBTI更新成功"));
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "MBTI更新失败"));
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "服务器内部错误"));
        }
    }

    /**
     * 修改当前用户名
     */
    @PutMapping("/api/user/username")
    public ResponseEntity<Object> changeUsername(
            @RequestBody Map<String, String> payload,
            HttpServletRequest request) {

        String newUsername = payload.get("username");
        try {
            String currentUserId = getUserIdFromRequest(request);
            if (currentUserId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "无效的用户凭证"));
            }

            boolean success = userService.changeUsername(currentUserId, newUsername);
            if (success) {
                // 注意：用户名修改成功后，当前 Token 中的 'sub' 字段 (用户名) 已过期
                // 最好是返回一个新的 Token，或者提示用户重新登录
                return ResponseEntity.ok(Map.of("message", "用户名修改成功，请重新登录以更新凭证"));
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "用户名修改失败"));
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "服务器内部错误"));
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
    @GetMapping("/api/user/mbti")
    public ResponseEntity<Object> getUserMbti(HttpServletRequest request) {
        try {
            String currentUserId = getUserIdFromRequest(request);
            if (currentUserId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "无效的用户凭证"));
            }

            String mbti = userService.getUserMbti(currentUserId);

            // 返回 JSON 格式: { "mbti": "INTJ" }
            return ResponseEntity.ok(Map.of("mbti", mbti != null ? mbti : ""));

        } catch (Exception e) {
            System.err.println("获取 MBTI 失败: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "获取 MBTI 失败"));
        }
    }
}