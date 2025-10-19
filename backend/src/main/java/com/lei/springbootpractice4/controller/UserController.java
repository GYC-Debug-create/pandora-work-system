package com.lei.springbootpractice4.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page; // 引入Page
import com.lei.springbootpractice4.pojo.UsersPojo;
import com.lei.springbootpractice4.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map; // 引入Map

@RestController
// @RequestMapping("/user") // 保持/user前缀用于登录和注册
public class UserController { // 移除类级别的 RequestMapping 或修改

    @Autowired
    private UserService userService;

    // POST /user/registerByAdmin (保持不变)
    @PostMapping("/user/registerByAdmin")
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
    @PostMapping("/user/login")
    public ResponseEntity<Object> login(@RequestBody UsersPojo user) {
        UsersPojo loginUser = userService.login(user.getUsername(), user.getPasswordHash());
        if (loginUser != null) {
            loginUser.setPasswordHash(null);
            return ResponseEntity.ok(loginUser);
        } else {
            return ResponseEntity.status(401).body("登录失败,用户名或密码错误");
        }
    }

    // --- 新增API端点 ---

    /**
     * 获取用户列表 (支持分页和查询)
     * 前端请求示例: GET /users?pageNum=1&pageSize=10&name=张
     */
    @GetMapping("/users")
    public ResponseEntity<Page<UsersPojo>> getUserList(@RequestParam Map<String, Object> params) {
        Page<UsersPojo> userPage = userService.getUserList(params);
        return ResponseEntity.ok(userPage);
    }

    /**
     * 更新用户信息
     * 前端请求示例: PUT /users/uuid-of-user
     */
    @PutMapping("/users/{id}")
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
    @DeleteMapping("/users/{id}")
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