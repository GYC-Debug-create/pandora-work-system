package com.lei.springbootpractice4.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lei.springbootpractice4.mapper.UserMapper;
import com.lei.springbootpractice4.pojo.UsersPojo;
import com.lei.springbootpractice4.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserMapper userMapper;
    @Override
    public boolean register(UsersPojo user) {
        QueryWrapper<UsersPojo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", user.getUsername());
        if(userMapper.selectCount(queryWrapper) > 0){
            return false;
        }else {
            userMapper.insert(user);
            return true;
        }
    }

    @Override
    public UsersPojo login(String username, String password) {
        QueryWrapper<UsersPojo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username).eq("password", password);
        UsersPojo user = userMapper.selectOne(queryWrapper);
        if(user!=null){
            return user;
        }else {
            return null;
        }
    }
}
