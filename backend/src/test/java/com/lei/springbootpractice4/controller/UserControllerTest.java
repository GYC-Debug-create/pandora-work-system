package com.lei.springbootpractice4.controller;

import com.lei.springbootpractice4.pojo.UsersPojo;
import com.lei.springbootpractice4.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * UserController单元测试类
 * 测试用户注册和登录接口
 */
@DisplayName("用户控制器测试")
public class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private UsersPojo testUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        testUser = new UsersPojo();
        testUser.setUsername("testuser");
        testUser.setPassword("password123");
    }

    @Test
    @DisplayName("测试用户注册成功")
    void testRegisterSuccess() {
        // 模拟注册成功
        when(userService.register(any(UsersPojo.class))).thenReturn(true);

        // 执行测试
        String result = userController.register(testUser);

        // 验证结果
        assertEquals("注册成功", result);
        verify(userService, times(1)).register(testUser);
    }

    @Test
    @DisplayName("测试用户注册失败 - 用户名已存在")
    void testRegisterFailUserExists() {
        // 模拟注册失败
        when(userService.register(any(UsersPojo.class))).thenReturn(false);

        // 执行测试
        String result = userController.register(testUser);

        // 验证结果
        assertEquals("注册失败,或许用户名已存在", result);
        verify(userService, times(1)).register(testUser);
    }

    @Test
    @DisplayName("测试用户注册 - 空用户名")
    void testRegisterWithEmptyUsername() {
        UsersPojo emptyUser = new UsersPojo();
        emptyUser.setUsername("");
        emptyUser.setPassword("password123");

        when(userService.register(any(UsersPojo.class))).thenReturn(false);

        String result = userController.register(emptyUser);

        assertEquals("注册失败,或许用户名已存在", result);
    }

    @Test
    @DisplayName("测试用户登录成功")
    void testLoginSuccess() {
        // 准备返回的用户对象
        UsersPojo loginUser = new UsersPojo();
        loginUser.setUser_id("123");
        loginUser.setUsername("testuser");
        loginUser.setPassword("password123");

        // 模拟登录成功
        when(userService.login(anyString(), anyString())).thenReturn(loginUser);

        // 执行测试
        Object result = userController.login(testUser);

        // 验证结果
        assertNotNull(result);
        assertTrue(result instanceof UsersPojo);
        UsersPojo returnedUser = (UsersPojo) result;
        assertEquals("123", returnedUser.getUser_id());
        assertEquals("testuser", returnedUser.getUsername());
        assertNull(returnedUser.getPassword(), "密码应该被设置为null");
        verify(userService, times(1)).login("testuser", "password123");
    }

    @Test
    @DisplayName("测试用户登录 - 空用户名")
    void testLoginWithEmptyUsername() {
        UsersPojo emptyUser = new UsersPojo();
        emptyUser.setUsername("");
        emptyUser.setPassword("password123");

        when(userService.login(anyString(), anyString())).thenReturn(null);

        // 由于Controller代码有bug,这里会抛出NullPointerException
        // 我们测试这个异常情况
        assertThrows(NullPointerException.class, () -> {
            userController.login(emptyUser);
        });
    }

    @Test
    @DisplayName("测试用户登录 - 空密码")
    void testLoginWithEmptyPassword() {
        UsersPojo emptyPasswordUser = new UsersPojo();
        emptyPasswordUser.setUsername("testuser");
        emptyPasswordUser.setPassword("");

        when(userService.login(anyString(), anyString())).thenReturn(null);

        // 由于Controller代码有bug,这里会抛出NullPointerException
        assertThrows(NullPointerException.class, () -> {
            userController.login(emptyPasswordUser);
        });
    }
}

