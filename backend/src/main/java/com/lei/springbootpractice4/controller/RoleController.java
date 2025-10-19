package com.lei.springbootpractice4.controller;

import com.lei.springbootpractice4.pojo.RolePojo;
import com.lei.springbootpractice4.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/roles") // 定义获取角色的API路径
public class RoleController {

    @Autowired
    private RoleService roleService;

    @GetMapping
    public List<RolePojo> getRoles() {
        return roleService.getAllRoles();
    }
}