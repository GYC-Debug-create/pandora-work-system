package com.lei.springbootpractice4.service.impl;

import com.lei.springbootpractice4.mapper.DepartmentMapper;
import com.lei.springbootpractice4.pojo.DepartmentPojo;
import com.lei.springbootpractice4.service.DepartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DepartmentServiceImpl implements DepartmentService {

    @Autowired
    private DepartmentMapper departmentMapper;

    @Override
    public List<DepartmentPojo> getAllDepartments() {
        return departmentMapper.selectList(null);
    }
}