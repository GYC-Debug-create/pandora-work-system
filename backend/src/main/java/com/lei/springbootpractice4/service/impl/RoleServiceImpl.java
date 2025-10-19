package com.lei.springbootpractice4.service.impl;

import com.lei.springbootpractice4.mapper.RoleMapper;
import com.lei.springbootpractice4.pojo.RolePojo;
import com.lei.springbootpractice4.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoleServiceImpl implements RoleService {

    @Autowired
    private RoleMapper roleMapper;

    @Override
    public List<RolePojo> getAllRoles() {
        // 返回所有角色，可以按需添加排序
        return roleMapper.selectList(null);
    }
}