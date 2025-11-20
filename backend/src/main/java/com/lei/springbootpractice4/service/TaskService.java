package com.lei.springbootpractice4.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page; // 引入 Page
import com.lei.springbootpractice4.pojo.TaskPojo;
import com.lei.springbootpractice4.pojo.UsersPojo; // 引入 UsersPojo
import java.util.List;
import java.util.Map; // 引入 Map

public interface TaskService {

    TaskPojo createTask(TaskPojo task, String creatorId, String location, String taskAttachmentsJson);

    boolean addMembersToTask(Integer taskId, List<String> userIds, String currentUserId,String location);

    /**
     * 根据当前用户权限和筛选条件获取主任务列表 (分页)
     * @param params 筛选参数 (pageNum, pageSize, taskName, status, priority, creator, responsible 等)
     * @param currentUser 当前登录的用户 (包含角色和部门信息)
     * @return 分页后的任务列表
     */
    Page<TaskPojo> getTaskList(Map<String, Object> params, UsersPojo currentUser);

    /**
     * 根据当前用户权限获取单个主任务详情
     * @param taskId 任务ID
     * @param currentUser 当前登录的用户
     * @return 任务详情 (包含子任务列表和成员列表)
     */
    TaskPojo getTaskById(Integer taskId, UsersPojo currentUser);

    /**
     * 更新主任务信息
     * @param taskId 任务ID
     * @param taskUpdateData 包含要更新字段的 Pojo
     * @param currentUser 当前登录的用户
     * @return 更新后的任务对象
     */
    TaskPojo updateTask(Integer taskId, TaskPojo taskUpdateData, UsersPojo currentUser,String location);
}