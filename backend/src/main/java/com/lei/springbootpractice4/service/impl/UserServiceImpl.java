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
import java.util.Objects;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RoleMapper roleMapper;


    @Autowired
    private DepartmentMapper departmentMapper; // <--- 用于查询部门名称


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
            if (user.getRoleId() != null) {
                RolePojo role = roleMapper.selectById(user.getRoleId());
                if (role != null) {
                    user.setRoleName(role.getRoleName());
                } else {
                    user.setRoleName("未知角色"); // 或者 null
                }
            }

            // 2. 填充部门名称
            if (user.getDepartmentId() != null) {
                DepartmentPojo department = departmentMapper.selectById(user.getDepartmentId());
                if (department != null) {
                    user.setDepartmentName(department.getDepartmentName());
                } else {
                    user.setDepartmentName("未知部门"); // 或者 null
                }
            }
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

            // status 字段已经从数据库读取，无需再设置
            // 如果数据库中 status 为 null，设置默认值
            if (user.getStatus() == null) {
                user.setStatus("活跃");
            }

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
    @Override
    public boolean changePassword(String userId, String oldPassword, String newPassword) {
        // 1. 获取当前用户
        UsersPojo user = userMapper.selectById(userId);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }

        // 2. 验证旧密码
        if (!passwordEncoder.matches(oldPassword, user.getPasswordHash())) {
            // 使用 SecurityException 或类似的异常来表示权限/验证失败
            throw new SecurityException("旧密码不正确");
        }

        // 3. 验证新密码 (例如：不能为空，不能太短等)
        if (!StringUtils.hasText(newPassword) || newPassword.length() < 6) {
            throw new IllegalArgumentException("新密码不能为空且长度至少为6位");
        }

        // 4. 加密并更新新密码
        String newHash = passwordEncoder.encode(newPassword);
        UsersPojo userToUpdate = new UsersPojo();
        userToUpdate.setUserId(userId);
        userToUpdate.setPasswordHash(newHash);

        return userMapper.updateById(userToUpdate) > 0;
    }

    @Override
    public boolean changeMbti(String userId, String newMbti) {
        if (!StringUtils.hasText(newMbti)) {
            throw new IllegalArgumentException("MBTI 不能为空");
        }
        // (可选) 校验 MBTI 格式是否合法

        UsersPojo userToUpdate = new UsersPojo();
        userToUpdate.setUserId(userId);
        userToUpdate.setMbtiType(newMbti);

        return userMapper.updateById(userToUpdate) > 0;
    }

    @Override
    public boolean changeUsername(String userId, String newUsername) {
        if (!StringUtils.hasText(newUsername)) {
            throw new IllegalArgumentException("用户名不能为空");
        }

        // 1. 检查新用户名是否已被他人占用
        QueryWrapper<UsersPojo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", newUsername);
        UsersPojo existingUser = userMapper.selectOne(queryWrapper);

        // 如果存在，且不是当前用户自己 (允许用户改成自己当前的名字，虽然没意义)
        if (existingUser != null && !Objects.equals(existingUser.getUserId(), userId)) {
            throw new IllegalArgumentException("该用户名已存在");
        }

        // 2. 更新用户名
        UsersPojo userToUpdate = new UsersPojo();
        userToUpdate.setUserId(userId);
        userToUpdate.setUsername(newUsername);

        return userMapper.updateById(userToUpdate) > 0;
    }
    @Override
    public String getUserMbti(String userId) {
        UsersPojo user = userMapper.selectById(userId);
        if (user != null) {
            return user.getMbtiType();
        }
        return null;
    }
}