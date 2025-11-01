package com.lei.springbootpractice4.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lei.springbootpractice4.mapper.*; // 引入所有需要的 Mapper
import com.lei.springbootpractice4.pojo.*; // 引入所有需要的 Pojo
import com.lei.springbootpractice4.service.LogService;
import com.lei.springbootpractice4.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils; // 引入 StringUtils

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class TaskServiceImpl implements TaskService {

    @Autowired
    private TaskMapper taskMapper;
    @Autowired
    private TaskMemberMapper taskMemberMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private RoleMapper roleMapper; // <--- 注入 RoleMapper
    @Autowired
    private SubTaskMapper subTaskMapper; // <--- 注入 SubTaskMapper
    @Autowired
    private LogService logService; // <--- 注入 LogService

    // --- createTask 和 addMembersToTask 方法保持不变 ---
    @Override
    @Transactional
    public TaskPojo createTask(TaskPojo task, String creatorId) {
        // (代码同上一版本，确保已添加 fillTaskNames 调用)
        task.setCreatorId(creatorId);
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        if ("TEAM".equals(task.getTaskType())) {
            if (task.getResponsiblePersonId() == null || task.getResponsiblePersonId().isBlank()) {
                throw new IllegalArgumentException("团队任务必须指定负责人 (responsiblePersonId)");
            }
            if (userMapper.selectById(task.getResponsiblePersonId()) == null) {
                throw new IllegalArgumentException("指定的负责人ID不存在: " + task.getResponsiblePersonId());
            }
            task.setStatus("PENDING_TEAM");
        } else {
            // 已根据用户需求调整为：个人任务不应调用此接口，而应调用 POST /api/subtasks
            // 强制设为 TEAM 或在 Controller 层面阻止
            throw new IllegalArgumentException("创建主任务接口仅用于创建团队任务，个人任务请使用子任务接口。");
        }
        if (task.getPriority() == null || task.getPriority().isBlank()) {
            task.setPriority("MEDIUM");
        }
        if (task.getStartDate() != null && task.getEndDate() != null && task.getStartDate().isAfter(task.getEndDate())) {
            throw new IllegalArgumentException("任务开始日期不能晚于结束日期");
        }

        taskMapper.insert(task);
        UsersPojo creator = userMapper.selectById(creatorId);
        fillTaskNames(task); // 填充名字
        logService.logTaskActivity(creator, task, "TASK_CREATED", "创建了");
        return task;
    }

    @Override
    @Transactional
    public boolean addMembersToTask(Integer taskId, List<String> userIds, String currentUserId) {

        TaskPojo task = taskMapper.selectById(taskId);
        if (task == null) throw new IllegalArgumentException("任务不存在: ID " + taskId);
        if (!"TEAM".equals(task.getTaskType())) throw new IllegalStateException("只有团队任务才能添加成员");
        if (!"PENDING_TEAM".equals(task.getStatus())) throw new IllegalStateException("任务状态不是待组队 (PENDING_TEAM)，无法添加成员");
        if (!Objects.equals(currentUserId, task.getResponsiblePersonId())) throw new SecurityException("只有任务负责人才能添加成员");
        if (userIds == null || userIds.isEmpty()) throw new IllegalArgumentException("成员列表 (userIds) 不能为空");

        LocalDateTime now = LocalDateTime.now();
        int insertedCount = 0; // 假设您计算了实际添加的数量
        for (String userId : userIds) {
            if (userMapper.selectById(userId) == null) throw new IllegalArgumentException("用户ID '" + userId + "' 不存在");
            QueryWrapper<TaskMemberPojo> existCheck = new QueryWrapper<>();
            existCheck.eq("task_id", taskId).eq("user_id", userId);
            if (taskMemberMapper.selectCount(existCheck) == 0) {
                taskMemberMapper.insert(new TaskMemberPojo(taskId, userId, now));
                insertedCount++;
            }
        }
        task.setStatus("IN_PROGRESS");
        task.setUpdatedAt(LocalDateTime.now());
        taskMapper.updateById(task);

        /*添加日志*/
        if (insertedCount > 0) {
            UsersPojo currentUser = userMapper.selectById(currentUserId);
            // (可以优化：获取成员名字列表)
            String suffix = String.format("为任务添加了 %d 名成员", insertedCount);
            logService.logTaskActivity(currentUser, task, "MEMBERS_ADDED", suffix);
        }
        return true;
    }


    // --- 新增：获取任务列表 (带权限) ---
    @Override
    public Page<TaskPojo> getTaskList(Map<String, Object> params, UsersPojo currentUser) {
        // 1. 获取分页参数
        long pageNum = Long.parseLong(params.getOrDefault("pageNum", "1").toString());
        long pageSize = Long.parseLong(params.getOrDefault("pageSize", "10").toString());
        Page<TaskPojo> page = new Page<>(pageNum, pageSize);

        // 2. 获取筛选参数
        String taskName = (String) params.get("taskName");
        String status = (String) params.get("status");
        String priority = (String) params.get("priority");
        String creator = (String) params.get("creator");
        String responsible = (String) params.get("responsible");
        // assignee: me 比较特殊，它适用于子任务。如果在这里用，可能指 "我是成员"
        String assignee = (String) params.get("assignee");

        QueryWrapper<TaskPojo> queryWrapper = new QueryWrapper<>();

        // 3. 应用权限规则 (核心)
        RolePojo userRole = roleMapper.selectById(currentUser.getRoleId());
        String roleName = (userRole != null) ? userRole.getRoleName() : "EMPLOYEE";
        String currentUserId = currentUser.getUserId();

        switch (roleName) {
            case "ADMIN":
            case "FOUNDER":
                // 管理员/创始人：无限制，可以看到所有任务
                System.out.println("权限：ADMIN/FOUNDER，查看所有。");
                break;
            case "DEPARTMENT_HEAD":
                // 部长：查看自己部门创建或负责的任务
                System.out.println("权限：DEPARTMENT_HEAD，查看本部门任务。");
                Integer departmentId = currentUser.getDepartmentId();
                if (departmentId == null) {
                    queryWrapper.eq("1", "0"); // 没部门的部长啥也看不见
                } else {
                    // 找出该部门所有用户的 ID
                    QueryWrapper<UsersPojo> userQuery = new QueryWrapper<UsersPojo>().eq("department_id", departmentId);
                    List<String> userIdsInDept = userMapper.selectList(userQuery).stream()
                            .map(UsersPojo::getUserId)
                            .collect(Collectors.toList());
                    if (userIdsInDept.isEmpty()) {
                        queryWrapper.eq("1", "0"); // 部门没人
                    } else {
                        queryWrapper.and(w -> w.in("creator_id", userIdsInDept)
                                .or()
                                .in("responsible_person_id", userIdsInDept));
                    }
                }
                break;
            case "TEAM_LEAD":
            case "EMPLOYEE":
            default:
                // 员工/团队长：查看与自己相关的 (创建、负责、参与)
                System.out.println("权限：EMPLOYEE/TEAM_LEAD，查看相关任务。");
                queryWrapper.and(w -> w.eq("creator_id", currentUserId)
                        .or()
                        .eq("responsible_person_id", currentUserId)
                        .or()
                        .apply("task_id IN (SELECT task_id FROM task_members WHERE user_id = {0})", currentUserId)
                );
                break;
        }

        // 4. 应用筛选条件
        if (StringUtils.hasText(taskName)) queryWrapper.like("task_name", taskName);
        if (StringUtils.hasText(status)) queryWrapper.eq("status", status);
        if (StringUtils.hasText(priority)) queryWrapper.eq("priority", priority);
        if ("me".equals(creator)) queryWrapper.eq("creator_id", currentUserId);
        if ("me".equals(responsible)) queryWrapper.eq("responsible_person_id", currentUserId);
        if ("me".equals(assignee)) { // 如果 "assignee=me" 指的是我是成员
            queryWrapper.apply("task_id IN (SELECT task_id FROM task_members WHERE user_id = {0})", currentUserId);
        }

        // 排序 (示例：按创建时间倒序)
        queryWrapper.orderByDesc("created_at");

        // 5. 执行查询
        Page<TaskPojo> taskPage = taskMapper.selectPage(page, queryWrapper);

        // 6. 填充额外信息 (名字、统计)
        taskPage.getRecords().forEach(this::fillTaskDetails); // 使用辅助方法

        return taskPage;
    }

    // --- 新增：获取任务详情 (带权限) ---
    @Override
    public TaskPojo getTaskById(Integer taskId, UsersPojo currentUser) {
        TaskPojo task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new IllegalArgumentException("任务不存在: ID " + taskId);
        }

        // 1. 权限校验
        if (!hasPermissionToView(task, currentUser)) {
            throw new SecurityException("无权查看该任务详情");
        }

        // 2. 填充详细信息 (名字、统计)
        fillTaskDetails(task);

        // 3. 填充子任务列表 (只获取第一层)
        QueryWrapper<SubTaskPojo> subTaskQuery = new QueryWrapper<>();
        subTaskQuery.eq("parent_task_id", taskId);
        subTaskQuery.isNull("parent_sub_task_id"); // 只查第一层
        List<SubTaskPojo> subTasks = subTaskMapper.selectList(subTaskQuery);
        // (可以按需填充子任务的 assignedToName)
        subTasks.forEach(this::fillSubTaskNames); // 使用 SubTaskService 的辅助方法
        task.setSubTasks(subTasks);

        // 4. 填充成员列表 (获取 UsersPojo 列表，而非 TaskMemberPojo)
        QueryWrapper<TaskMemberPojo> memberQuery = new QueryWrapper<>();
        memberQuery.eq("task_id", taskId);
        List<String> memberUserIds = taskMemberMapper.selectList(memberQuery).stream()
                .map(TaskMemberPojo::getUserId)
                .collect(Collectors.toList());
        if (!memberUserIds.isEmpty()) {
            List<UsersPojo> members = userMapper.selectBatchIds(memberUserIds);
            members.forEach(m -> m.setPasswordHash(null)); // 清除密码
            task.setMembers(members);
        } else {
            task.setMembers(Collections.emptyList());
        }

        return task;
    }

    // --- 新增：更新任务信息 (带权限) ---
    @Override
    @Transactional
    public TaskPojo updateTask(Integer taskId, TaskPojo taskUpdateData, UsersPojo currentUser) {
        TaskPojo existingTask = taskMapper.selectById(taskId);
        if (existingTask == null) {
            throw new IllegalArgumentException("要更新的任务不存在: ID " + taskId);
        }

        // 1. 权限校验 (创建者或负责人)
        String currentUserId = currentUser.getUserId();
        if (!Objects.equals(currentUserId, existingTask.getCreatorId()) &&
                !Objects.equals(currentUserId, existingTask.getResponsiblePersonId())) {
            throw new SecurityException("只有创建者或负责人才能修改任务");
        }

        // 2. 更新字段
        // (使用 Mybatis-Plus 的 updateById，它默认只更新非 null 字段)
        // (但为保险起见，我们手动设置允许更新的字段)
        TaskPojo taskToUpdate = new TaskPojo();
        taskToUpdate.setTaskId(taskId); // 必须设置 ID
        taskToUpdate.setTaskName(taskUpdateData.getTaskName());
        taskToUpdate.setDescription(taskUpdateData.getDescription());
        taskToUpdate.setResponsiblePersonId(taskUpdateData.getResponsiblePersonId()); // 允许更改负责人
        taskToUpdate.setTaskType(taskUpdateData.getTaskType());
        taskToUpdate.setPriority(taskUpdateData.getPriority());
        taskToUpdate.setStatus(taskUpdateData.getStatus());
        taskToUpdate.setStartDate(taskUpdateData.getStartDate());
        taskToUpdate.setEndDate(taskUpdateData.getEndDate());
        taskToUpdate.setUpdatedAt(LocalDateTime.now()); // 强制更新时间

        // 校验日期
        if (taskToUpdate.getStartDate() != null && taskToUpdate.getEndDate() != null && taskToUpdate.getStartDate().isAfter(taskToUpdate.getEndDate())) {
            throw new IllegalArgumentException("任务开始日期不能晚于结束日期");
        }
        // 校验新负责人是否存在
        if (taskToUpdate.getResponsiblePersonId() != null && userMapper.selectById(taskToUpdate.getResponsiblePersonId()) == null) {
            throw new IllegalArgumentException("指定的新负责人ID不存在");
        }

        taskMapper.updateById(taskToUpdate); // updateById 会自动忽略 null 字段
        logService.logTaskActivity(currentUser, existingTask, "TASK_UPDATED", "更新了");

        // 3. 返回更新后的完整数据
        return getTaskById(taskId, currentUser); // 调用详情接口获取最新数据
    }


    // --- 辅助方法 ---

    // 填充任务创建者和负责人的名字
    private void fillTaskNames(TaskPojo task) {
        if (task == null) return;
        if (task.getCreatorId() != null && task.getCreatorName() == null) {
            UsersPojo creator = userMapper.selectById(task.getCreatorId());
            if (creator != null) task.setCreatorName(creator.getFullName() != null ? creator.getFullName() : creator.getUsername());
        }
        if (task.getResponsiblePersonId() != null && task.getResponsiblePersonName() == null) {
            UsersPojo responsible = userMapper.selectById(task.getResponsiblePersonId());
            if (responsible != null) task.setResponsiblePersonName(responsible.getFullName() != null ? responsible.getFullName() : responsible.getUsername());
        }
    }

    // 填充任务的统计数据（子任务数、成员数）
    private void fillTaskDetails(TaskPojo task) {
        if (task == null) return;
        Integer taskId = task.getTaskId();

        // 填充名字
        fillTaskNames(task);

        // 填充成员数
        QueryWrapper<TaskMemberPojo> memberCountQuery = new QueryWrapper<>();
        memberCountQuery.eq("task_id", taskId);
        task.setMemberCount(taskMemberMapper.selectCount(memberCountQuery).intValue());

        // 填充子任务统计
        QueryWrapper<SubTaskPojo> subTaskQuery = new QueryWrapper<>();
        subTaskQuery.eq("parent_task_id", taskId);
        List<SubTaskPojo> allSubTasks = subTaskMapper.selectList(subTaskQuery); // 获取所有层级的？（目前只获取了第一层）

        // API 文档建议统计所有子任务，但目前查询只获取了第一层
        // 暂时只统计第一层
        task.setTotalSubTaskCount(allSubTasks.size());
        task.setCompletedSubTaskCount((int) allSubTasks.stream().filter(st -> "COMPLETED".equals(st.getStatus())).count());
    }

    // 辅助方法：检查用户是否有权查看任务
    private boolean hasPermissionToView(TaskPojo task, UsersPojo currentUser) {
        if (task == null || currentUser == null) return false;

        RolePojo userRole = roleMapper.selectById(currentUser.getRoleId());
        String roleName = (userRole != null) ? userRole.getRoleName() : "EMPLOYEE";
        String currentUserId = currentUser.getUserId();

        // 1. 管理员/创始人
        if ("ADMIN".equals(roleName) || "FOUNDER".equals(roleName)) {
            return true;
        }

        // 2. 部长
        if ("DEPARTMENT_HEAD".equals(roleName)) {
            Integer userDeptId = currentUser.getDepartmentId();
            if (userDeptId != null) {
                UsersPojo creator = userMapper.selectById(task.getCreatorId());
                UsersPojo responsible = userMapper.selectById(task.getResponsiblePersonId());
                if ((creator != null && userDeptId.equals(creator.getDepartmentId())) ||
                        (responsible != null && userDeptId.equals(responsible.getDepartmentId()))) {
                    return true;
                }
            }
        }

        // 3. 员工/团队长 (检查是否相关)
        if (Objects.equals(currentUserId, task.getCreatorId()) ||
                Objects.equals(currentUserId, task.getResponsiblePersonId())) {
            return true;
        }
        // 检查是否为成员
        QueryWrapper<TaskMemberPojo> memberCheck = new QueryWrapper<>();
        memberCheck.eq("task_id", task.getTaskId()).eq("user_id", currentUserId).last("LIMIT 1");
        if (taskMemberMapper.selectCount(memberCheck) > 0) {
            return true;
        }

        return false; // 默认无权限
    }

    // 辅助方法：填充子任务的名字
    private void fillSubTaskNames(SubTaskPojo subTask) {
        if (subTask == null) return;
        if (subTask.getAssignedToId() != null && subTask.getAssignedToName() == null) {
            UsersPojo assignedUser = userMapper.selectById(subTask.getAssignedToId());
            if (assignedUser != null) {
                subTask.setAssignedToName(assignedUser.getFullName() != null ? assignedUser.getFullName() : assignedUser.getUsername());
            }
        }
        // 可以在这里也填充 creatorName (如果需要)
    }
}