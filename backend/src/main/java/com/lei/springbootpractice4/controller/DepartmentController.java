package com.lei.springbootpractice4.controller;

import com.lei.springbootpractice4.pojo.DepartmentPojo;
import com.lei.springbootpractice4.service.DepartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/departments")
public class DepartmentController {

    @Autowired
    private DepartmentService departmentService;

    @GetMapping
    public List<DepartmentPojo> getDepartments() {
        return departmentService.getAllDepartments();
    }
}