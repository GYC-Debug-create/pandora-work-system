package com.lei.springbootpractice4.service;

import com.lei.springbootpractice4.pojo.UsersPojo;

public interface UserService {
    boolean registerByAdmin(UsersPojo user);
    UsersPojo login(String username, String password);
}
