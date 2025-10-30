// 文件路径: backend/src/main/java/com/lei/springbootpractice4/service/ViewService.java
package com.lei.springbootpractice4.service;

import com.lei.springbootpractice4.pojo.SubTaskPojo;
import java.time.LocalDate;
import java.util.List;

public interface ViewService {

    /**
     * 获取指定月份中，当前用户负责的所有子任务
     * @param currentUserId 当前登录用户的 ID
     * @param dateInMonth 指定月份中的任意一天
     * @return 子任务列表
     */
    List<SubTaskPojo> getMonthViewTasks(String currentUserId, LocalDate dateInMonth);

    /**
     * 获取指定周中，当前用户创建并分配给自己的子任务
     * @param currentUserId 当前登录用户的 ID
     * @param dateInWeek 指定周中的任意一天
     * @return 子任务列表
     */
    List<SubTaskPojo> getWeekViewTasks(String currentUserId, LocalDate dateInWeek);

    /**
     * 获取指定日期，当前用户创建并分配给自己的子任务
     * @param currentUserId 当前登录用户的 ID
     * @param date 指定日期
     * @return 子任务列表
     */
    List<SubTaskPojo> getDayViewTasks(String currentUserId, LocalDate date);
}