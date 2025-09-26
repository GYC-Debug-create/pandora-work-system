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
    @PostMapping("/register")
    public String register(@RequestBody UsersPojo user) {
        boolean registSucces = userService.register(user);
        if (registSucces) {
            return "注册成功";
        } else {
            return "注册失败,或许用户名已存在";
        }
    }


    @PostMapping("/login")
    public Object login(@RequestBody UsersPojo user){
        UsersPojo loginUser = userService.login(user.getUsername(), user.getPassword());
        loginUser.setPassword(null);
        if (loginUser != null) {
            return loginUser;
        } else {
            return "登录失败,用户名或密码错误";
        }
    }
}
