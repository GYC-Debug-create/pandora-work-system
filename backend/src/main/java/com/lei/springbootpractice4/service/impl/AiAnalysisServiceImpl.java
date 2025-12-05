// 文件路径: backend/src/main/java/com/lei/springbootpractice4/service/impl/AiAnalysisServiceImpl.java
package com.lei.springbootpractice4.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lei.springbootpractice4.mapper.RoleMapper;
import com.lei.springbootpractice4.mapper.SubTaskMapper;
import com.lei.springbootpractice4.mapper.UserMapper;
import com.lei.springbootpractice4.pojo.RolePojo;
import com.lei.springbootpractice4.pojo.SubTaskPojo;
import com.lei.springbootpractice4.pojo.UsersPojo;
import com.lei.springbootpractice4.service.AiAnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class AiAnalysisServiceImpl implements AiAnalysisService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private SubTaskMapper subTaskMapper;

    @Autowired
    private RoleMapper roleMapper;

    @Override
    public Map<String, Object> getAiAnalysisData(String targetUserId, UsersPojo currentUser) {

        // 1. 获取目标用户信息 (提前获取，用于权限判断)
        UsersPojo targetUser = userMapper.selectById(targetUserId);
        if (targetUser == null) {
            throw new IllegalArgumentException("目标用户不存在: ID " + targetUserId);
        }

        // 2. 权限校验 (核心修改部分)
        checkPermission(currentUser, targetUser);

        // 3. 获取最近12条子任务 (按 created_at 降序)
        // (保持不变：获取被分配的任务)
        QueryWrapper<SubTaskPojo> subTaskQuery = new QueryWrapper<>();
        subTaskQuery.eq("assigned_to_id", targetUserId);
        subTaskQuery.orderByDesc("created_at");
        subTaskQuery.last("LIMIT 12"); // 仍保持只获取最近12条给AI分析

        List<SubTaskPojo> recentSubTasks = subTaskMapper.selectList(subTaskQuery);

        // 4. 组装成 DTO (保持不变)
        Map<String, Object> userData = new HashMap<>();
        userData.put("user_id", targetUser.getUserId());
        userData.put("mbti_type", targetUser.getMbtiType());

        List<Map<String, Object>> taskList = new ArrayList<>();
        for (SubTaskPojo st : recentSubTasks) {
            Map<String, Object> taskData = new HashMap<>();
            taskData.put("sub_task_id", st.getSubTaskId());
            taskData.put("sub_task_name", st.getSubTaskName());
            taskData.put("status", st.getStatus());
            taskData.put("created_at", st.getCreatedAt());
            taskData.put("end_time", st.getEndTime());
            taskList.add(taskData);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("user", userData);
        response.put("tasks", taskList);

        return response;
    }

    /**
     * 辅助方法：权限检查
     */
    private void checkPermission(UsersPojo currentUser, UsersPojo targetUser) {
        // 规则 1: 用户访问自己的数据 -> 允许
        if (Objects.equals(currentUser.getUserId(), targetUser.getUserId())) {
            return;
        }

        RolePojo role = roleMapper.selectById(currentUser.getRoleId());
        String roleName = (role != null) ? role.getRoleName() : "EMPLOYEE";

        // 规则 2: 管理员 (ADMIN) 或 创始人 (FOUNDER) -> 允许所有
        if ("ADMIN".equals(roleName) || "FOUNDER".equals(roleName)) {
            return;
        }

        // 规则 3: 部门老总 (DEPARTMENT_HEAD) 访问本部门员工 -> 允许
        if ("DEPARTMENT_HEAD".equals(roleName)) {
            if (currentUser.getDepartmentId() != null &&
                    Objects.equals(currentUser.getDepartmentId(), targetUser.getDepartmentId())) {
                return;
            }
        }

        // 其他情况 -> 拒绝
        throw new SecurityException("无权访问该用户的 AI 分析数据");
    }
}