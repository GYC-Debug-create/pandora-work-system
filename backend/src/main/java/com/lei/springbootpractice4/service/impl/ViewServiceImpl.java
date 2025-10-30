// 文件路径: backend/src/main/java/com/lei/springbootpractice4/service/impl/ViewServiceImpl.java
package com.lei.springbootpractice4.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lei.springbootpractice4.mapper.SubTaskMapper;
import com.lei.springbootpractice4.mapper.UserMapper;
import com.lei.springbootpractice4.pojo.SubTaskPojo;
import com.lei.springbootpractice4.pojo.UsersPojo;
import com.lei.springbootpractice4.service.ViewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ViewServiceImpl implements ViewService {

    @Autowired
    private SubTaskMapper subTaskMapper;

    @Autowired
    private UserMapper userMapper;

    /**
     * 月视图：获取上级分配给我的任务 (assigned_to_id == me AND creator_id != me)
     */
    @Override
    public List<SubTaskPojo> getMonthViewTasks(String currentUserId, LocalDate dateInMonth) {
        LocalDate firstDayOfMonth = dateInMonth.with(TemporalAdjusters.firstDayOfMonth());
        LocalDate lastDayOfMonth = dateInMonth.with(TemporalAdjusters.lastDayOfMonth());
        LocalDateTime monthStart = firstDayOfMonth.atStartOfDay();
        LocalDateTime monthEnd = lastDayOfMonth.atTime(LocalTime.MAX);

        QueryWrapper<SubTaskPojo> queryWrapper = new QueryWrapper<>();
        queryWrapper
                .eq("assigned_to_id", currentUserId) // 规则：我负责的
                .ne("creator_id", currentUserId)     // --- 新增规则：排除我自己创建的 ---
                .isNotNull("start_time")
                .isNotNull("end_time")
                .ne("status", "COMPLETED")
                .ne("status", "CANCELLED")
                .and(wrapper -> wrapper
                        .lt("start_time", monthEnd)
                        .gt("end_time", monthStart));

        List<SubTaskPojo> tasks = subTaskMapper.selectList(queryWrapper);
        tasks.forEach(this::fillNames);
        return tasks;
    }

    /**
     * 周视图：获取我自己创建给自己的任务 (creator_id == me AND assigned_to_id == me)
     */
    @Override
    public List<SubTaskPojo> getWeekViewTasks(String currentUserId, LocalDate dateInWeek) {
        LocalDate firstDayOfWeek = dateInWeek.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate lastDayOfWeek = dateInWeek.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
        LocalDateTime weekStart = firstDayOfWeek.atStartOfDay();
        LocalDateTime weekEnd = lastDayOfWeek.atTime(LocalTime.MAX);

        QueryWrapper<SubTaskPojo> queryWrapper = new QueryWrapper<>();
        queryWrapper
                .eq("assigned_to_id", currentUserId) // 规则：我负责的
                .eq("creator_id", currentUserId)     // 规则：我创建的
                .isNotNull("start_time")
                .isNotNull("end_time")
                .ne("status", "COMPLETED")
                .ne("status", "CANCELLED")
                .and(wrapper -> wrapper
                        .lt("start_time", weekEnd)
                        .gt("end_time", weekStart));

        List<SubTaskPojo> tasks = subTaskMapper.selectList(queryWrapper);
        tasks.forEach(this::fillNames);
        return tasks;
    }

    /**
     * 日视图：获取我自己创建给自己的任务 (creator_id == me AND assigned_to_id == me)
     */
    @Override
    public List<SubTaskPojo> getDayViewTasks(String currentUserId, LocalDate date) {
        LocalDateTime dayStart = date.atStartOfDay();
        LocalDateTime dayEnd = date.atTime(LocalTime.MAX);

        QueryWrapper<SubTaskPojo> queryWrapper = new QueryWrapper<>();
        queryWrapper
                .eq("assigned_to_id", currentUserId) // 规则：我负责的
                .eq("creator_id", currentUserId)     // 规则：我创建的
                .isNotNull("start_time")
                .isNotNull("end_time")
                .ne("status", "COMPLETED")
                .ne("status", "CANCELLED")
                .and(wrapper -> wrapper
                        .lt("start_time", dayEnd)
                        .gt("end_time", dayStart));

        List<SubTaskPojo> tasks = subTaskMapper.selectList(queryWrapper);
        tasks.forEach(this::fillNames);
        return tasks;
    }

    // 辅助方法：填充名字
    private void fillNames(SubTaskPojo subTask) {
        if (subTask == null) return;
        if (subTask.getAssignedToId() != null && subTask.getAssignedToName() == null) {
            UsersPojo assignedUser = userMapper.selectById(subTask.getAssignedToId());
            if (assignedUser != null) {
                subTask.setAssignedToName(assignedUser.getFullName() != null ? assignedUser.getFullName() : assignedUser.getUsername());
            }
        }
        if (subTask.getCreatorId() != null && subTask.getCreatorName() == null) {
            UsersPojo creatorUser = userMapper.selectById(subTask.getCreatorId());
            if (creatorUser != null) {
                subTask.setCreatorName(creatorUser.getFullName() != null ? creatorUser.getFullName() : creatorUser.getUsername());
            }
        }
    }
}