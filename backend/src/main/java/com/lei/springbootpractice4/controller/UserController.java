package com.lei.springbootpractice4.controller;

import com.lei.springbootpractice4.pojo.UsersPojo;
import com.lei.springbootpractice4.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;

    // This endpoint should be secured for ADMIN role in a real scenario
    @PostMapping("/registerByAdmin")
    public String registerByAdmin(@RequestBody UsersPojo user) {
        // Assume the raw password comes in the passwordHash field from the DTO
        String rawPassword = user.getPasswordHash();
        user.setPasswordHash(rawPassword);

        boolean registSuccess = userService.registerByAdmin(user);
        if (registSuccess) {
            return "用户创建成功";
        } else {
            return "创建失败, 用户名已存在";
        }
    }

    @PostMapping("/login")
    public Object login(@RequestBody UsersPojo user){
        UsersPojo loginUser = userService.login(user.getUsername(), user.getPasswordHash());

        if (loginUser != null) {
            loginUser.setPasswordHash(null); // Never send password hash to client
            return loginUser;
        } else {
            return "登录失败,用户名或密码错误";
        }
    }
}