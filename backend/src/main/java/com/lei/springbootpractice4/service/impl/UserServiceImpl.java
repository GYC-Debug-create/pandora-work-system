package com.lei.springbootpractice4.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lei.springbootpractice4.mapper.UserMapper;
import com.lei.springbootpractice4.pojo.UsersPojo;
import com.lei.springbootpractice4.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public boolean registerByAdmin(UsersPojo user) {
        QueryWrapper<UsersPojo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", user.getUsername());
        if(userMapper.selectCount(queryWrapper) > 0){
            return false; // User already exists
        } else {
            // Encode password before saving
            user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));
            userMapper.insert(user);
            return true;
        }
    }

    @Override
    public UsersPojo login(String username, String password) {
        QueryWrapper<UsersPojo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        UsersPojo user = userMapper.selectOne(queryWrapper);

        if(user != null && passwordEncoder.matches(password, user.getPasswordHash())){
            return user; // Password matches
        } else {
            return null; // User not found or password incorrect
        }
    }
}