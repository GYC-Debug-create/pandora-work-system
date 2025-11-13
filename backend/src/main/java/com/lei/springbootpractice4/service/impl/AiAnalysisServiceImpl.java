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

        // 1. 权限校验
        RolePojo role = roleMapper.selectById(currentUser.getRoleId());
        String roleName = (role != null) ? role.getRoleName() : "EMPLOYEE";

        // 既不是本人，也不是 ADMIN 或 FOUNDER
        if (!Objects.equals(currentUser.getUserId(), targetUserId) &&
                !"ADMIN".equals(roleName) &&
                !"FOUNDER".equals(roleName)) {

            throw new SecurityException("无权访问该用户的 AI 分析数据");
        }

        // 2. 获取目标用户信息
        UsersPojo targetUser = userMapper.selectById(targetUserId);
        if (targetUser == null) {
            throw new IllegalArgumentException("目标用户不存在: ID " + targetUserId);
        }

        // 3. 获取最近12条子任务 (按 created_at 降序)
        QueryWrapper<SubTaskPojo> subTaskQuery = new QueryWrapper<>();
        subTaskQuery.eq("assigned_to_id", targetUserId); // 分配给该用户的
        subTaskQuery.orderByDesc("created_at");
        subTaskQuery.last("LIMIT 12"); // 获取最近12条

        List<SubTaskPojo> recentSubTasks = subTaskMapper.selectList(subTaskQuery);

        // 4. 组装成您需要的 DTO (Data Transfer Object) 格式

        // 组装 user 部分
        Map<String, Object> userData = new HashMap<>();
        userData.put("user_id", targetUser.getUserId());
        userData.put("mbti_type", targetUser.getMbtiType());

        // 组装 tasks 列表
        List<Map<String, Object>> taskList = new ArrayList<>();
        for (SubTaskPojo st : recentSubTasks) {
            Map<String, Object> taskData = new HashMap<>();
            taskData.put("sub_task_id", st.getSubTaskId());
            taskData.put("sub_task_name", st.getSubTaskName());

            // (可选) 转换状态码为中文，如果 AI 需要的话
            // String statusDisplay = convertStatusToChinese(st.getStatus());
            // taskData.put("status", statusDisplay);
            taskData.put("status", st.getStatus()); // 暂时用原始状态码 (COMPLETED, TODO 等)

            taskData.put("created_at", st.getCreatedAt());
            taskData.put("end_time", st.getEndTime()); // 结束时间
            taskList.add(taskData);
        }

        // 组装最终响应
        Map<String, Object> response = new HashMap<>();
        response.put("user", userData);
        response.put("tasks", taskList);

        return response;
    }

    // (可选的辅助方法)
    // private String convertStatusToChinese(String status) {
    //     switch (status) {
    //         case "COMPLETED": return "已完成";
    //         case "IN_PROGRESS": return "进行中";
    //         case "TODO": return "待办";
    //         case "PENDING_APPROVAL": return "待审批";
    //         case "REJECTED": return "已驳回";
    //         default: return status;
    //     }
    // }
}