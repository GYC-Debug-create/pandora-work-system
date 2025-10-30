package com.lei.springbootpractice4.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page; // 引入Page
import com.lei.springbootpractice4.config.JwtUtil;
import com.lei.springbootpractice4.pojo.UsersPojo;
import com.lei.springbootpractice4.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
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

    // POST /user/registerByAdmin (保持不变)
    @PostMapping("/api/user/registerByAdmin")
    public String registerByAdmin(@RequestBody UsersPojo user) {
        String rawPassword = user.getPasswordHash();
        user.setPasswordHash(rawPassword);
        boolean registSuccess = userService.registerByAdmin(user);
        if (registSuccess) {
            return "用户创建成功";
        } else {
            return "创建失败, 用户名已存在";
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
    // --- 新增API端点 ---

    /**
     * 获取用户列表 (支持分页和查询)
     * 前端请求示例: GET /users?pageNum=1&pageSize=10&name=张
     */
    @GetMapping("/api/users")
    public ResponseEntity<Page<UsersPojo>> getUserList(@RequestParam Map<String, Object> params) {
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
}