package com.lei.springbootpractice4.service;

import com.lei.springbootpractice4.mapper.UserMapper;
import com.lei.springbootpractice4.pojo.UsersPojo;
import com.lei.springbootpractice4.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * UserService单元测试类
 * 测试用户注册和登录功能
 */
@DisplayName("用户服务测试")
public class UserServiceTest {

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("测试用户注册成功 - 用户名不存在")
    void testRegisterSuccess() {
        // 准备测试数据
        UsersPojo newUser = new UsersPojo();
        newUser.setUsername("testuser");
        newUser.setPassword("password123");

        // 模拟用户名不存在的情况
        when(userMapper.selectCount(any(QueryWrapper.class))).thenReturn(0L);
        when(userMapper.insert(any(UsersPojo.class))).thenReturn(1);

        // 执行测试
        boolean result = userService.register(newUser);

        // 验证结果
        assertTrue(result, "用户注册应该成功");
        verify(userMapper, times(1)).selectCount(any(QueryWrapper.class));
        verify(userMapper, times(1)).insert(newUser);
    }

    @Test
    @DisplayName("测试用户注册失败 - 用户名已存在")
    void testRegisterFailUserExists() {
        // 准备测试数据
        UsersPojo existingUser = new UsersPojo();
        existingUser.setUsername("existinguser");
        existingUser.setPassword("password123");

        // 模拟用户名已存在的情况
        when(userMapper.selectCount(any(QueryWrapper.class))).thenReturn(1L);

        // 执行测试
        boolean result = userService.register(existingUser);

        // 验证结果
        assertFalse(result, "用户名已存在时注册应该失败");
        verify(userMapper, times(1)).selectCount(any(QueryWrapper.class));
        verify(userMapper, never()).insert(any(UsersPojo.class));
    }

    @Test
    @DisplayName("测试用户注册 - 空用户名")
    void testRegisterWithEmptyUsername() {
        // 准备测试数据
        UsersPojo user = new UsersPojo();
        user.setUsername("");
        user.setPassword("password123");

        // 模拟用户名不存在
        when(userMapper.selectCount(any(QueryWrapper.class))).thenReturn(0L);
        when(userMapper.insert(any(UsersPojo.class))).thenReturn(1);

        // 执行测试
        boolean result = userService.register(user);

        // 验证结果
        assertTrue(result, "空用户名也应该能注册(业务逻辑未限制)");
    }

    @Test
    @DisplayName("测试用户登录成功 - 用户名和密码正确")
    void testLoginSuccess() {
        // 准备测试数据
        String username = "testuser";
        String password = "<REDACTED>";
        UsersPojo expectedUser = new UsersPojo();
        expectedUser.setUser_id("123");
        expectedUser.setUsername(username);
        expectedUser.setPassword(password);

        // 模拟登录成功
        when(userMapper.selectOne(any(QueryWrapper.class))).thenReturn(expectedUser);

        // 执行测试
        UsersPojo result = userService.login(username, password);

        // 验证结果
        assertNotNull(result, "登录成功应该返回用户对象");
        assertEquals(username, result.getUsername(), "用户名应该匹配");
        assertEquals(password, result.getPassword(), "密码应该匹配");
        verify(userMapper, times(1)).selectOne(any(QueryWrapper.class));
    }

    @Test
    @DisplayName("测试用户登录失败 - 用户名或密码错误")
    void testLoginFailInvalidCredentials() {
        // 准备测试数据
        String username = "wronguser";
        String password = "wrongpassword";

        // 模拟登录失败
        when(userMapper.selectOne(any(QueryWrapper.class))).thenReturn(null);

        // 执行测试
        UsersPojo result = userService.login(username, password);

        // 验证结果
        assertNull(result, "用户名或密码错误时应该返回null");
        verify(userMapper, times(1)).selectOne(any(QueryWrapper.class));
    }

    @Test
    @DisplayName("测试用户登录 - 空用户名")
    void testLoginWithEmptyUsername() {
        // 准备测试数据
        String username = "";
        String password = "<REDACTED>";

        // 模拟查询结果
        when(userMapper.selectOne(any(QueryWrapper.class))).thenReturn(null);

        // 执行测试
        UsersPojo result = userService.login(username, password);

        // 验证结果
        assertNull(result, "空用户名登录应该失败");
    }

    @Test
    @DisplayName("测试用户登录 - 空密码")
    void testLoginWithEmptyPassword() {
        // 准备测试数据
        String username = "testuser";
        String password = "";

        // 模拟查询结果
        when(userMapper.selectOne(any(QueryWrapper.class))).thenReturn(null);

        // 执行测试
        UsersPojo result = userService.login(username, password);

        // 验证结果
        assertNull(result, "空密码登录应该失败");
    }
}

