package com.lei.springbootpractice4.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page; // 引入Page
import com.lei.springbootpractice4.mapper.DepartmentMapper;
import com.lei.springbootpractice4.mapper.RoleMapper;
import com.lei.springbootpractice4.mapper.UserMapper;
import com.lei.springbootpractice4.pojo.DepartmentPojo;
import com.lei.springbootpractice4.pojo.RolePojo;
import com.lei.springbootpractice4.pojo.UsersPojo;
import com.lei.springbootpractice4.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils; // 引入StringUtils

import java.util.Map; // 引入Map

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RoleMapper roleMapper;

    @Autowired // 注入 DepartmentMapper
    private DepartmentMapper departmentMapper;
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
            // 查询关联的角色和部门信息 (如果需要返回给前端)
            // user.setRole(...)
            // user.setDepartment(...)
            return user; // Password matches
        } else {
            return null; // User not found or password incorrect
        }
    }

    // --- 新增方法的实现 ---

    // backend/src/main/java/com/lei/springbootpractice4/service/impl/UserServiceImpl.java

// ... (其他 imports 和类定义) ...

    @Override
    public Page<UsersPojo> getUserList(Map<String, Object> params) {
        // 获取分页参数，确保类型安全
        long pageNum = 1;
        long pageSize = 10;
        try {
            pageNum = Long.parseLong(params.getOrDefault("pageNum", "1").toString());
            pageSize = Long.parseLong(params.getOrDefault("pageSize", "10").toString());
        } catch (NumberFormatException e) {
            System.err.println("Invalid pagination parameters received.");
        }
        Page<UsersPojo> page = new Page<>(pageNum, pageSize);

        QueryWrapper<UsersPojo> queryWrapper = new QueryWrapper<>();

        // 获取并处理查询参数
        String name = params.get("name") instanceof String ? (String) params.get("name") : null;
        String email = params.get("email") instanceof String ? (String) params.get("email") : null;
        String roleName = params.get("role") instanceof String ? (String) params.get("role") : null; // 获取的是角色名称

        // 添加查询条件
        if (StringUtils.hasText(name)) {
            queryWrapper.like("username", name);
        }
        if (StringUtils.hasText(email)) {
            queryWrapper.like("email", email);
        }

        // --- 健壮的角色查询逻辑 ---
        if (StringUtils.hasText(roleName)) {
            System.out.println("Querying for role name: " + roleName); // 增加日志方便调试
            QueryWrapper<RolePojo> roleQuery = new QueryWrapper<>();
            roleQuery.eq("role_name", roleName).last("LIMIT 1");
            RolePojo role = roleMapper.selectOne(roleQuery);
            if (role != null) {
                System.out.println("Found role ID: " + role.getRoleId()); // 增加日志
                queryWrapper.eq("role_id", role.getRoleId());
            } else {
                // 如果传入了前端存在但后端roles表没有的角色名（理论上不应发生），则查询无结果
                System.err.println("Role name not found in database: " + roleName);
                queryWrapper.eq("role_id", -1); // 确保没有匹配
            }
        }
        // --- 角色查询逻辑结束 ---

        Page<UsersPojo> userPage = userMapper.selectPage(page, queryWrapper);
        userPage.getRecords().forEach(user -> user.setPasswordHash(null));

        userPage.getRecords().forEach(user -> {
            user.setPasswordHash(null); // 清除密码哈希

            // 查询角色名
            if (user.getRoleId() != null) {
                RolePojo role = roleMapper.selectById(user.getRoleId());
                if (role != null) {
                    user.setRoleName(role.getRoleName());
                }
            }

            // 查询部门名
            if (user.getDepartmentId() != null) {
                DepartmentPojo department = departmentMapper.selectById(user.getDepartmentId());
                if (department != null) {
                    user.setDepartmentName(department.getDepartmentName());
                }
            }

            // 设置状态 (当前硬编码，后续可从数据库读取或根据逻辑判断)
            user.setStatus("活跃");

            // 确保 createdAt 字段有值 (MyBatis Plus 应该会自动填充，这里做个检查)
            // if (user.getCreatedAt() == null) { user.setCreatedAt(LocalDateTime.now()); }

        });
        System.out.println("Returning " + userPage.getTotal() + " users."); // 增加日志
        return userPage;
    }

// ... (其他方法) ...

    @Override
    public boolean updateUser(String userId, UsersPojo user) {
        // 不允许通过此接口修改密码，将密码字段设为null
        user.setPasswordHash(null);
        user.setUserId(userId); // 确保ID设置正确
        // TODO: 添加业务逻辑，例如不允许修改创始人的角色等
        int updatedRows = userMapper.updateById(user);
        return updatedRows > 0;
    }

    @Override
    public boolean deleteUser(String userId) {
        // TODO: 添加业务逻辑，例如不允许删除创始人账号
        UsersPojo userToDelete = userMapper.selectById(userId);
        if (userToDelete != null) {
            // 假设 'ADMIN' 是管理员角色，且不能删除
            // if ("ADMIN".equals(userToDelete.getRole())) { // 需要先查询角色名
            //     return false; // 或者抛出业务异常
            // }
            int deletedRows = userMapper.deleteById(userId);
            return deletedRows > 0;
        }
        return false; // 用户不存在
    }
}