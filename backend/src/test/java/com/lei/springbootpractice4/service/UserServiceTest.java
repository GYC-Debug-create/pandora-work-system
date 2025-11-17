package com.lei.springbootpractice4.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lei.springbootpractice4.mapper.DepartmentMapper;
import com.lei.springbootpractice4.mapper.RoleMapper;
import com.lei.springbootpractice4.mapper.UserMapper;
import com.lei.springbootpractice4.pojo.RolePojo;
import com.lei.springbootpractice4.pojo.UsersPojo;
import com.lei.springbootpractice4.service.impl.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class) // 使用 Mockito 扩展
public class UserServiceTest {

    @Mock // 模拟 UserMapper
    private UserMapper userMapper;

    @Mock // 模拟 PasswordEncoder
    private PasswordEncoder passwordEncoder;

    @Mock // 模拟 RoleMapper
    private RoleMapper roleMapper;

    @Mock // 模拟 DepartmentMapper
    private DepartmentMapper departmentMapper;

    @InjectMocks // 将上面的 Mock 对象注入到 UserServiceImpl 中
    private UserServiceImpl userService;

    @Test
    public void testLoginSuccess() {
        // 1. 准备数据
        String username = "testuser";
        String rawPassword = "password123";
        String encodedPassword = "encodedPassword123";

        UsersPojo mockUser = new UsersPojo();
        mockUser.setUserId("1");
        mockUser.setUsername(username);
        mockUser.setPasswordHash(encodedPassword);
        mockUser.setRoleId(1);

        RolePojo mockRole = new RolePojo();
        mockRole.setRoleName("TEST_ROLE");

        // 2. 定义 Mock 行为 (当调用什么方法时，返回什么结果)
        // 当查询用户时，返回 mockUser
        when(userMapper.selectOne(any(QueryWrapper.class))).thenReturn(mockUser);
        // 当验证密码时，返回 true
        when(passwordEncoder.matches(rawPassword, encodedPassword)).thenReturn(true);
        // 当查询角色时，返回 mockRole
        when(roleMapper.selectById(1)).thenReturn(mockRole);

        // 3. 执行测试方法
        UsersPojo result = userService.login(username, rawPassword);

        // 4. 验证结果 (断言)
        assertNotNull(result, "登录成功时不应返回 null");
        assertEquals(username, result.getUsername(), "返回的用户名应一致");
        assertEquals("TEST_ROLE", result.getRoleName(), "角色名称应被填充");
    }

    @Test
    public void testLoginFailed_WrongPassword() {
        // 1. 准备数据
        String username = "testuser";
        String wrongPassword = "wrong";
        String encodedPassword = "encodedPassword123";

        UsersPojo mockUser = new UsersPojo();
        mockUser.setUsername(username);
        mockUser.setPasswordHash(encodedPassword);

        // 2. 定义 Mock 行为
        when(userMapper.selectOne(any(QueryWrapper.class))).thenReturn(mockUser);
        // 密码验证返回 false
        when(passwordEncoder.matches(wrongPassword, encodedPassword)).thenReturn(false);

        // 3. 执行测试
        UsersPojo result = userService.login(username, wrongPassword);

        // 4. 验证结果
        assertNull(result, "密码错误时应返回 null");
    }
}