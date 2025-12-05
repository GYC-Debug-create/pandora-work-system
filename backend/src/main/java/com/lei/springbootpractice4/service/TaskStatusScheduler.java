package com.lei.springbootpractice4.service;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.lei.springbootpractice4.mapper.SubTaskMapper;
import com.lei.springbootpractice4.mapper.TaskMapper;
import com.lei.springbootpractice4.pojo.SubTaskPojo;
import com.lei.springbootpractice4.pojo.TaskPojo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

// 改名为 TaskStatusScheduler 以避免与 Spring 的 TaskScheduler 冲突
@Component
public class TaskStatusScheduler {

    @Autowired
    private TaskMapper taskMapper;

    @Autowired
    private SubTaskMapper subTaskMapper;

    /**
     * 每分钟执行一次检查
     */
    @Scheduled(cron = "0 * * * * ?")
    @Transactional
    public void checkOverdueTasks() {
        LocalDateTime now = LocalDateTime.now();
        LocalDate today = LocalDate.now();

        // 1. 更新过期的主任务 (Tasks)
        UpdateWrapper<TaskPojo> taskUpdate = new UpdateWrapper<>();
        taskUpdate.set("status", "OVERDUE")
                .set("updated_at", now)
                .ne("status", "COMPLETED")
                .ne("status", "CANCELLED")
                .ne("status", "OVERDUE")
                .lt("end_date", today); // 截止日期小于今天

        int tasksUpdated = taskMapper.update(null, taskUpdate);
        if (tasksUpdated > 0) {
            System.out.println("定时任务: 已将 " + tasksUpdated + " 个逾期主任务标记为 OVERDUE");
        }

        // 2. 更新过期的子任务 (SubTasks)
        UpdateWrapper<SubTaskPojo> subTaskUpdate = new UpdateWrapper<>();
        subTaskUpdate.set("status", "OVERDUE")
                .set("updated_at", now)
                .in("status", "TODO", "IN_PROGRESS")
                .lt("end_time", now); // 结束时间小于当前时间

        int subTasksUpdated = subTaskMapper.update(null, subTaskUpdate);
        if (subTasksUpdated > 0) {
            System.out.println("定时任务: 已将 " + subTasksUpdated + " 个逾期子任务标记为 OVERDUE");
        }
    }
}