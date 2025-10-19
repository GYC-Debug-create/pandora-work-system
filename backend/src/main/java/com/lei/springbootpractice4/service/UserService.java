package com.lei.springbootpractice4.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page; // 引入Page
import com.lei.springbootpractice4.pojo.UsersPojo;
import java.util.Map; // 引入Map

public interface UserService {
    boolean registerByAdmin(UsersPojo user);
    UsersPojo login(String username, String password);

    // --- 新增方法 ---
    /**
     * 分页并根据条件查询用户列表
     * @param params 查询参数 (包含 pageNum, pageSize, name, email, role 等)
     * @return 分页结果
     */
    Page<UsersPojo> getUserList(Map<String, Object> params);

    /**
     * 更新用户信息 (不包括密码)
     * @param userId 要更新的用户ID
     * @param user 包含更新信息的用户对象
     * @return 是否成功
     */
    boolean updateUser(String userId, UsersPojo user);

    /**
     * 删除用户
     * @param userId 要删除的用户ID
     * @return 是否成功
     */
    boolean deleteUser(String userId);
}