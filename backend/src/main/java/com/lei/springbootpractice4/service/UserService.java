package com.lei.springbootpractice4.service;

import com.lei.springbootpractice4.pojo.UsersPojo;

public interface UserService {
    boolean register(UsersPojo user);
    UsersPojo login(String username, String password);
}
