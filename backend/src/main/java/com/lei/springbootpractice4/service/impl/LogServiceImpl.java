package com.lei.springbootpractice4.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lei.springbootpractice4.mapper.*;
import com.lei.springbootpractice4.pojo.*;
import com.lei.springbootpractice4.service.LogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class LogServiceImpl implements LogService {

    @Autowired
    private LogMapper logMapper;
    // 我们不需要 UserMapper 等，因为调用方会把对象传过来
    @Autowired
    private UserMapper userMapper; // <--- 注入 UserMapper
    @Autowired
    private TaskMapper taskMapper; // <--- 注入 TaskMapper
    @Autowired
    private SubTaskMapper subTaskMapper; // <--- 注入 SubTaskMapper
    @Autowired
    private RoleMapper roleMapper; // <--- 注入 RoleMapper


    @Override
    public String getUserDisplayName(UsersPojo user) {
        if (user == null) return "未知用户";
        return user.getFullName() != null ? user.getFullName() : user.getUsername();
    }

    /**
     * 创建日志（在新的事务中执行，以确保即使主业务回滚，日志也能（可选地）保留）
     * 或者使用 REQUIRES_NEW 确保日志提交
     */
    @Override
    @Transactional // 确保日志写入在独立事务中
    public void logTaskActivity(UsersPojo user, TaskPojo task, String activityType, String descriptionSuffix,String location) {
        try {
            String userName = getUserDisplayName(user);
            String taskName = task.getTaskName() != null ? "'" + task.getTaskName() + "'" : "";

            String description = String.format("%s %s 主任务 %s", userName, descriptionSuffix, taskName).trim();

            LogPojo log = new LogPojo(
                    user.getUserId(),
                    task.getTaskId(),
                    null, // 主任务活动，无子任务ID
                    activityType,
                    location,
                    description
            );
            logMapper.insert(log);
        } catch (Exception e) {
            // 捕获所有异常，防止日志记录失败导致主业务回滚
            System.err.println("--- 日志记录失败 (Task) ---");
            e.printStackTrace();
            System.err.println("---------------------------");
        }
    }

    @Override
    @Transactional
    public void logSubTaskActivity(UsersPojo user, SubTaskPojo subTask, String activityType, String descriptionPrefix,String location) {
        try {
            String userName = getUserDisplayName(user);
            String subTaskName = subTask.getSubTaskName() != null ? "'" + subTask.getSubTaskName() + "'" : "";

            String description = String.format("%s %s 子任务 %s", userName, descriptionPrefix, subTaskName).trim();

            LogPojo log = new LogPojo(
                    user.getUserId(),
                    subTask.getParentTaskId(), // 关联主任务ID
                    subTask.getSubTaskId(), // 关联子任务ID
                    activityType,
                    location,
                    description
            );
            logMapper.insert(log);
        } catch (Exception e) {
            System.err.println("--- 日志记录失败 (SubTask) ---");
            e.printStackTrace();
            System.err.println("---------------------------");
        }
    }

    @Override
    public Page<LogPojo> getLogs(Map<String, Object> params, UsersPojo currentUser) {
        // 1. 获取分页参数
        long pageNum = Long.parseLong(params.getOrDefault("pageNum", "1").toString());
        long pageSize = Long.parseLong(params.getOrDefault("pageSize", "10").toString());
        Page<LogPojo> page = new Page<>(pageNum, pageSize);

        QueryWrapper<LogPojo> queryWrapper = new QueryWrapper<>();

        // 2. 应用权限规则
        RolePojo userRole = roleMapper.selectById(currentUser.getRoleId());
        String roleName = (userRole != null) ? userRole.getRoleName() : "EMPLOYEE";
        String currentUserId = currentUser.getUserId();

        switch (roleName) {
            case "ADMIN":
            case "FOUNDER":
                // 管理员/创始人：无限制
                break;
            case "DEPARTMENT_HEAD":
            case "TEAM_LEAD":
                // 部长/团队长：查看自己部门成员的日志
                Integer departmentId = currentUser.getDepartmentId();
                if (departmentId != null) {
                    QueryWrapper<UsersPojo> userQuery = new QueryWrapper<UsersPojo>().eq("department_id", departmentId);
                    List<String> userIdsInDept = userMapper.selectList(userQuery).stream()
                            .map(UsersPojo::getUserId)
                            .collect(Collectors.toList());
                    if (!userIdsInDept.isEmpty()) {
                        queryWrapper.in("user_id", userIdsInDept);
                    } else {
                        queryWrapper.eq("user_id", currentUserId); // 部门没人，至少看自己的
                    }
                } else {
                    queryWrapper.eq("user_id", currentUserId); // 没部门，只看自己的
                }
                break;
            case "EMPLOYEE":
            default:
                // 员工：只能看自己的
                queryWrapper.eq("user_id", currentUserId);
                break;
        }

        // 3. 应用筛选条件 (来自 params)
        if (params.containsKey("userId") && StringUtils.hasText(params.get("userId").toString())) {
            // (如果权限允许) 筛选特定用户
            // 注意：这里可能需要与权限逻辑合并，例如员工不能查询他人的 userId
            queryWrapper.eq("user_id", params.get("userId").toString());
        }
        if (params.containsKey("taskId") && StringUtils.hasText(params.get("taskId").toString())) {
            queryWrapper.eq("related_task_id", Integer.parseInt(params.get("taskId").toString()));
        }
        if (params.containsKey("subTaskId") && StringUtils.hasText(params.get("subTaskId").toString())) {
            queryWrapper.eq("related_sub_task_id", Integer.parseInt(params.get("subTaskId").toString()));
        }
        if (params.containsKey("date") && StringUtils.hasText(params.get("date").toString())) {
            // 筛选特定日期
            try {
                LocalDate date = LocalDate.parse(params.get("date").toString());
                queryWrapper.between("created_at", date.atStartOfDay(), date.atTime(LocalTime.MAX));
            } catch (Exception e) { /* 忽略无效日期 */ }
        }

        // 排序：按创建时间倒序
        queryWrapper.orderByDesc("created_at");

        // 4. 执行分页查询
        Page<LogPojo> logPage = logMapper.selectPage(page, queryWrapper);

        // 5. 填充名字 (N+1 查询优化)
        if (logPage.getRecords() != null && !logPage.getRecords().isEmpty()) {
            // 收集所有需要查询的 ID
            Set<String> userIds = logPage.getRecords().stream().map(LogPojo::getUserId).filter(Objects::nonNull).collect(Collectors.toSet());
            Set<Integer> taskIds = logPage.getRecords().stream().map(LogPojo::getRelatedTaskId).filter(Objects::nonNull).collect(Collectors.toSet());
            Set<Integer> subTaskIds = logPage.getRecords().stream().map(LogPojo::getRelatedSubTaskId).filter(Objects::nonNull).collect(Collectors.toSet());

            // 批量查询
            Map<String, UsersPojo> userMap = userIds.isEmpty() ? Map.of() :
                    userMapper.selectBatchIds(userIds).stream().collect(Collectors.toMap(UsersPojo::getUserId, Function.identity()));
            Map<Integer, TaskPojo> taskMap = taskIds.isEmpty() ? Map.of() :
                    taskMapper.selectBatchIds(taskIds).stream().collect(Collectors.toMap(TaskPojo::getTaskId, Function.identity()));
            Map<Integer, SubTaskPojo> subTaskMap = subTaskIds.isEmpty() ? Map.of() :
                    subTaskMapper.selectBatchIds(subTaskIds).stream().collect(Collectors.toMap(SubTaskPojo::getSubTaskId, Function.identity()));

            // 遍历并设置名字
            for (LogPojo log : logPage.getRecords()) {
                if (log.getUserId() != null && userMap.containsKey(log.getUserId())) {
                    log.setUserName(getUserDisplayName(userMap.get(log.getUserId())));
                }
                if (log.getRelatedTaskId() != null && taskMap.containsKey(log.getRelatedTaskId())) {
                    log.setRelatedTaskName(taskMap.get(log.getRelatedTaskId()).getTaskName());
                }
                if (log.getRelatedSubTaskId() != null && subTaskMap.containsKey(log.getRelatedSubTaskId())) {
                    log.setRelatedSubTaskName(subTaskMap.get(log.getRelatedSubTaskId()).getSubTaskName());
                }
            }
        }

        return logPage;
    }
    @Override
    @Transactional
    public LogPojo updateLogDescription(Integer logId, String newDescription, UsersPojo currentUser) {
        // 1. 获取日志
        LogPojo log = logMapper.selectById(logId);
        if (log == null) {
            throw new IllegalArgumentException("日志不存在: ID " + logId);
        }

        // 2. 权限校验：复用“查看权限”的逻辑
        // 规则：如果我有权查看这条日志，我就有权修改它
        if (!canViewLog(log, currentUser)) {
            throw new SecurityException("无权修改该日志 (您无权查看此日志)");
        }

        // 3. 执行更新
        log.setDescription(newDescription);
        // 也可以更新个时间，或者记录个修改人，看需求。这里只更新描述。
        logMapper.updateById(log);

        // 4. 填充名字以便返回给前端展示
        fillLogNames(log);

        return log;
    }


    private boolean canViewLog(LogPojo log, UsersPojo currentUser) {
        RolePojo userRole = roleMapper.selectById(currentUser.getRoleId());
        String roleName = (userRole != null) ? userRole.getRoleName() : "EMPLOYEE";
        String currentUserId = currentUser.getUserId();

        // 1. 管理员/创始人：看所有
        if ("ADMIN".equals(roleName) || "FOUNDER".equals(roleName)) {
            return true;
        }

        // 2. 部门老总/团队长：看本部门成员的日志
        if ("DEPARTMENT_HEAD".equals(roleName) || "TEAM_LEAD".equals(roleName)) {
            Integer currentDeptId = currentUser.getDepartmentId();
            if (currentDeptId != null) {
                // 获取日志所有者的信息
                UsersPojo logOwner = userMapper.selectById(log.getUserId());
                // 如果日志所有者存在，且与当前用户同部门，则允许
                if (logOwner != null && Objects.equals(currentDeptId, logOwner.getDepartmentId())) {
                    return true;
                }
                // 或者日志就是自己的
                if (Objects.equals(log.getUserId(), currentUserId)) {
                    return true;
                }
            }
        }

        // 3. 员工：只看自己的
        if (Objects.equals(log.getUserId(), currentUserId)) {
            return true;
        }

        return false;
    }

    // --- 辅助方法：填充单条日志的名字 (复用 getLogs 里的逻辑) ---
    private void fillLogNames(LogPojo log) {
        if (log.getUserId() != null) {
            UsersPojo u = userMapper.selectById(log.getUserId());
            if (u != null) log.setUserName(getUserDisplayName(u));
        }
        if (log.getRelatedTaskId() != null) {
            TaskPojo t = taskMapper.selectById(log.getRelatedTaskId());
            if (t != null) log.setRelatedTaskName(t.getTaskName());
        }
        if (log.getRelatedSubTaskId() != null) {
            SubTaskPojo st = subTaskMapper.selectById(log.getRelatedSubTaskId());
            if (st != null) log.setRelatedSubTaskName(st.getSubTaskName());
        }
    }
}