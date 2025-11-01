package com.lei.springbootpractice4.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lei.springbootpractice4.pojo.LogPojo;
import com.lei.springbootpractice4.pojo.SubTaskPojo;
import com.lei.springbootpractice4.pojo.TaskPojo;
import com.lei.springbootpractice4.pojo.UsersPojo;

import java.util.Map;

public interface LogService {

    /**
     * 内部方法：获取用户的可显示名称 (全名或用户名)
     * @param user 用户对象
     * @return 显示名称
     */
    String getUserDisplayName(UsersPojo user);

    /**
     * 创建一条与主任务相关的日志
     * @param user 执行操作的用户
     * @param task 相关的主任务
     * @param activityType 活动类型
     * @param descriptionSuffix 描述的后半部分 (例如: "添加了 3 名成员")
     */
    void logTaskActivity(UsersPojo user, TaskPojo task, String activityType, String descriptionSuffix);

    /**
     * 创建一条与子任务相关的日志
     * @param user 执行操作的用户
     * @param subTask 相关的子任务
     * @param activityType 活动类型
     * @param descriptionPrefix 描述的前半部分 (例如: "提交了", "审批通过了")
     */
    void logSubTaskActivity(UsersPojo user, SubTaskPojo subTask, String activityType, String descriptionPrefix);

    // (后续我们将在这里添加 GET /api/logs 的读取方法)

    /**
     * 根据权限和条件分页查询日志
     * @param params 筛选参数 (pageNum, pageSize, userId, taskId, subTaskId, date 等)
     * @param currentUser 当前登录的用户
     * @return 填充了名字的分页日志数据
     */
    Page<LogPojo> getLogs(Map<String, Object> params, UsersPojo currentUser); // <--- 新增方法
}