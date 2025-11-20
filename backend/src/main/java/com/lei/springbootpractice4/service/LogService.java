package com.lei.springbootpractice4.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lei.springbootpractice4.pojo.LogPojo;
import com.lei.springbootpractice4.pojo.SubTaskPojo;
import com.lei.springbootpractice4.pojo.TaskPojo;
import com.lei.springbootpractice4.pojo.UsersPojo;
import java.util.Map;

public interface LogService {

    String getUserDisplayName(UsersPojo user);

    /**
     * 创建一条与主任务相关的日志
     * @param location (新增) 执行操作的地理位置
     */
    void logTaskActivity(UsersPojo user, TaskPojo task, String activityType, String descriptionSuffix, String location);

    /**
     * 创建一条与子任务相关的日志
     * @param location (新增) 执行操作的地理位置
     */
    void logSubTaskActivity(UsersPojo user, SubTaskPojo subTask, String activityType, String descriptionPrefix, String location);

    Page<LogPojo> getLogs(Map<String, Object> params, UsersPojo currentUser);
}