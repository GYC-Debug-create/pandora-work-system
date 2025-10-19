package com.lei.springbootpractice4.service;

import com.lei.springbootpractice4.pojo.DepartmentPojo;
import java.util.List;

public interface DepartmentService {
    List<DepartmentPojo> getAllDepartments();
}