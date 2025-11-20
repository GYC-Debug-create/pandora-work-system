// 文件路径: backend/src/main/java/com/lei/springbootpractice4/service/impl/SubTaskServiceImpl.java
package com.lei.springbootpractice4.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lei.springbootpractice4.mapper.SubTaskMapper;
import com.lei.springbootpractice4.mapper.TaskMapper;
import com.lei.springbootpractice4.mapper.UserMapper;
import com.lei.springbootpractice4.mapper.RoleMapper;
import com.lei.springbootpractice4.pojo.RolePojo;
import com.lei.springbootpractice4.pojo.SubTaskPojo;
import com.lei.springbootpractice4.pojo.TaskPojo;
import com.lei.springbootpractice4.pojo.UsersPojo;
import com.lei.springbootpractice4.service.LogService;
import com.lei.springbootpractice4.service.SubTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Service
public class SubTaskServiceImpl implements SubTaskService {

    @Autowired
    private SubTaskMapper subTaskMapper;
    @Autowired
    private TaskMapper taskMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private RoleMapper roleMapper;
    @Autowired
    private LogService logService; // <--- 注入 LogService

    // --- createSubTask 方法 (保持不变) ---
    @Override
    @Transactional
    public SubTaskPojo createSubTask(SubTaskPojo subTask, String creatorId, String location) {
        subTask.setCreatorId(creatorId);
        subTask.setStatus("TODO");
        subTask.setCreatedAt(LocalDateTime.now());
        subTask.setUpdatedAt(LocalDateTime.now());

        Integer parentTaskId = subTask.getParentTaskId();
        Integer parentSubTaskId = subTask.getParentSubTaskId();
        String assignedToId = subTask.getAssignedToId();
        LocalDateTime startTime = subTask.getStartTime();
        LocalDateTime endTime = subTask.getEndTime();

        if (parentTaskId != null && parentSubTaskId != null) {
            throw new IllegalArgumentException("不能同时指定父主任务ID和父子任务ID");
        }

        if (parentTaskId != null || parentSubTaskId != null) { // 分解任务
            if (assignedToId == null || assignedToId.isBlank()) {
                throw new IllegalArgumentException("分解任务时必须指定负责人 (assignedToId)");
            }
            if (userMapper.selectById(assignedToId) == null) {
                throw new IllegalArgumentException("指定的负责人ID不存在: " + assignedToId);
            }
            if (parentTaskId != null) { // 主任务分解
                TaskPojo parentTask = taskMapper.selectById(parentTaskId);
                if (parentTask == null) throw new IllegalArgumentException("无效的父主任务ID: " + parentTaskId);
                if (!Objects.equals(creatorId, parentTask.getResponsiblePersonId())) throw new SecurityException("只有主任务的负责人才能分解任务");
            } else { // 二次分解 (parentSubTaskId != null)
                SubTaskPojo parentSubTask = subTaskMapper.selectById(parentSubTaskId);
                if (parentSubTask == null) throw new IllegalArgumentException("无效的父子任务ID: " + parentSubTaskId);
                if (!Objects.equals(creatorId, parentSubTask.getAssignedToId())) throw new SecurityException("只有父子任务的负责人才能进行二次分解");
                if (!Objects.equals(assignedToId, creatorId)) throw new IllegalArgumentException("二次分解的任务只能分配给自己");
                subTask.setParentTaskId(parentSubTask.getParentTaskId());
            }
        } else { // 个人任务
            if (assignedToId == null || assignedToId.isBlank()) {
                subTask.setAssignedToId(creatorId);
                assignedToId = creatorId;
            } else if (!Objects.equals(assignedToId, creatorId)) {
                throw new IllegalArgumentException("个人任务的负责人只能是创建者本人");
            }
        }

        if (startTime != null && endTime != null) {
            if (startTime.isAfter(endTime)) {
                throw new IllegalArgumentException("子任务的开始时间不能晚于结束时间");
            }
            QueryWrapper<SubTaskPojo> conflictCheckWrapper = new QueryWrapper<>();
            conflictCheckWrapper
                    .eq("assigned_to_id", assignedToId)
                    .ne("status", "COMPLETED")
                    .ne("status", "CANCELLED")
                    .ne(parentSubTaskId != null, "sub_task_id", parentSubTaskId)
                    .and(wrapper -> wrapper
                            .lt("start_time", endTime)
                            .gt("end_time", startTime));
            List<SubTaskPojo> conflictingTasks = subTaskMapper.selectList(conflictCheckWrapper);
            if (!conflictingTasks.isEmpty()) {
                SubTaskPojo conflict = conflictingTasks.get(0);
                String errorMessage = String.format(
                        "时间冲突：与任务 '%s' (%s - %s) 时间重叠。",
                        conflict.getSubTaskName(), conflict.getStartTime(), conflict.getEndTime()
                );
                throw new IllegalStateException(errorMessage);
            }
        } else {
            System.out.println("警告: 子任务未指定开始或结束时间，跳过时间冲突检查。");
        }

        subTaskMapper.insert(subTask);
        fillNames(subTask);
        UsersPojo creator = userMapper.selectById(creatorId);
        String prefix = (subTask.getParentTaskId() != null || subTask.getParentSubTaskId() != null) ? "分解出" : "创建了";
        logService.logSubTaskActivity(creator, subTask, "SUBTASK_CREATED", prefix, location); // <--- 传递 location
        return subTask;
    }

    // --- submitDeliverable 方法 (保持不变) ---
    @Override
    @Transactional
    public SubTaskPojo submitDeliverable(Integer subTaskId, String deliverableContentJson, String currentUserId,String location) {
        SubTaskPojo subTask = subTaskMapper.selectById(subTaskId);
        if (subTask == null) {
            throw new IllegalArgumentException("子任务不存在");
        }
        if (!Objects.equals(currentUserId, subTask.getAssignedToId())) {
            throw new SecurityException("只有子任务的负责人才能提交交付物");
        }
        String currentStatus = subTask.getStatus();
        if (!("TODO".equals(currentStatus) || "IN_PROGRESS".equals(currentStatus) || "REJECTED".equals(currentStatus))) {
            throw new IllegalStateException("当前任务状态 (" + currentStatus + ") 不允许提交交付物");
        }
        subTask.setDeliverableContent(deliverableContentJson);
        subTask.setStatus("PENDING_APPROVAL");
        subTask.setUpdatedAt(LocalDateTime.now());
        subTaskMapper.updateById(subTask);
        fillNames(subTask);
        //日志添加
        UsersPojo currentUser = userMapper.selectById(currentUserId);
        logService.logSubTaskActivity(currentUser, subTask, "SUBTASK_SUBMITTED", "提交了", location);

        return subTask;
    }

    // --- reviewSubTask 方法 (修改：调用递归检查) ---
    @Override
    @Transactional
    public SubTaskPojo reviewSubTask(Integer subTaskId, boolean approved, String comment, String reviewerId, String location) {
        SubTaskPojo subTask = subTaskMapper.selectById(subTaskId);
        if (subTask == null) {
            throw new IllegalArgumentException("子任务不存在");
        }
        // 简化权限校验
        if (Objects.equals(reviewerId, subTask.getAssignedToId())) {
            throw new SecurityException("不能审批自己提交的任务");
        }
        // ... (其他权限校验) ...

        if (!"PENDING_APPROVAL".equals(subTask.getStatus())) {
            throw new IllegalStateException("当前任务状态不是待审批，无法执行审批操作");
        }

        UsersPojo reviewer = userMapper.selectById(reviewerId);
        UsersPojo assignee = userMapper.selectById(subTask.getAssignedToId());
        String activityType;
        String descriptionPrefix;

        if (approved) {
            subTask.setStatus("COMPLETED");
            subTask.setCompletedAt(LocalDateTime.now());
            subTask.setApprovalComment(comment);
            activityType = "SUBTASK_APPROVED";
            descriptionPrefix = "审批通过了";
        } else {
            if (comment == null || comment.isBlank()) {
                throw new IllegalArgumentException("驳回时必须填写评论");
            }
            subTask.setStatus("REJECTED");
            subTask.setApprovalComment(comment);
            subTask.setCompletedAt(null);
            activityType = "SUBTASK_REJECTED";
            descriptionPrefix = "驳回了";
        }
        subTask.setUpdatedAt(LocalDateTime.now());
        subTaskMapper.updateById(subTask);

        logService.logSubTaskActivity(reviewer, subTask, activityType, descriptionPrefix,location);
        logService.logSubTaskActivity(assignee, subTask, "SUBTASK_COMPLETED", "完成了",location);
        // --- 日志结束 ---

        // --- 修改：调用递归检查 ---
        if (approved) {
            // 检查是否需要自动完成父级（包括父子任务或主任务）
            checkAndTriggerParentCompletion(subTask,location);
        }
        // --- 修改结束 ---

        fillNames(subTask);
        return subTask;
    }

    // --- completeSubTask 方法 (修改：调用递归检查) ---
    @Override
    @Transactional
    public SubTaskPojo completeSubTask(Integer subTaskId, String currentUserId,String location) {
        SubTaskPojo subTask = subTaskMapper.selectById(subTaskId);
        if (subTask == null) {
            throw new IllegalArgumentException("子任务不存在");
        }
        boolean isPersonalTask = subTask.getParentTaskId() == null && subTask.getParentSubTaskId() == null;
        if (!Objects.equals(currentUserId, subTask.getAssignedToId())) {
            throw new SecurityException("只有任务负责人才能将任务标记为完成");
        }
        if (!isPersonalTask) {
            throw new IllegalStateException("只有个人创建的任务才能直接标记为完成，分解的任务需要提交审批");
        }
        String currentStatus = subTask.getStatus();
        if ("COMPLETED".equals(currentStatus)) {
            throw new IllegalStateException("任务已经是完成状态");
        }

        subTask.setStatus("COMPLETED");
        subTask.setCompletedAt(LocalDateTime.now());
        subTask.setUpdatedAt(LocalDateTime.now());
        subTaskMapper.updateById(subTask);

        UsersPojo currentUser = userMapper.selectById(currentUserId);
        logService.logSubTaskActivity(currentUser, subTask, "SUBTASK_COMPLETED_MANUALLY", "将个人任务标记为",location);

        // --- 修改：调用递归检查 ---
        // 个人任务没有父级，所以 checkAndTriggerParentCompletion(subTask) 会安全地不做任何事
        checkAndTriggerParentCompletion(subTask,location);
        // --- 修改结束 ---

        fillNames(subTask);
        return subTask;
    }

    // --- getSubTaskById 方法 (保持不变) ---
    @Override
    public SubTaskPojo getSubTaskById(Integer subTaskId, UsersPojo currentUser) {
        SubTaskPojo subTask = subTaskMapper.selectById(subTaskId);
        if (subTask == null) {
            throw new IllegalArgumentException("子任务不存在: ID " + subTaskId);
        }
        if (!hasPermissionToViewSubTask(subTask, currentUser)) {
            throw new SecurityException("无权查看该子任务详情");
        }
        fillNames(subTask);
        loadDirectChildren(subTask); // 只加载一级
        return subTask;
    }

    // --- 辅助方法 (保持不变) ---
    private boolean hasPermissionToViewSubTask(SubTaskPojo subTask, UsersPojo currentUser) {
        // ... (代码保持不变) ...
        if (subTask == null || currentUser == null) return false;
        RolePojo userRole = roleMapper.selectById(currentUser.getRoleId());
        String roleName = (userRole != null) ? userRole.getRoleName() : "EMPLOYEE";
        String currentUserId = currentUser.getUserId();
        if ("ADMIN".equals(roleName) || "FOUNDER".equals(roleName)) return true;
        if (Objects.equals(currentUserId, subTask.getAssignedToId())) return true;
        if (Objects.equals(currentUserId, subTask.getCreatorId())) return true;
        if (subTask.getParentTaskId() != null) {
            TaskPojo parentTask = taskMapper.selectById(subTask.getParentTaskId());
            if (parentTask != null && Objects.equals(currentUserId, parentTask.getResponsiblePersonId())) return true;
        }
        return false;
    }

    private void loadDirectChildren(SubTaskPojo parentSubTask) {
        // ... (代码保持不变) ...
        if (parentSubTask == null) return;
        QueryWrapper<SubTaskPojo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("parent_sub_task_id", parentSubTask.getSubTaskId());
        List<SubTaskPojo> children = subTaskMapper.selectList(queryWrapper);
        if (children == null || children.isEmpty()) {
            parentSubTask.setChildren(Collections.emptyList());
            return;
        }
        for (SubTaskPojo child : children) {
            fillNames(child);
            child.setChildren(Collections.emptyList());
        }
        parentSubTask.setChildren(children);
    }

    private void fillNames(SubTaskPojo subTask) {
        // ... (代码保持不变) ...
        if (subTask == null) return;
        if (subTask.getAssignedToId() != null && subTask.getAssignedToName() == null) {
            UsersPojo assignedUser = userMapper.selectById(subTask.getAssignedToId());
            if (assignedUser != null) subTask.setAssignedToName(assignedUser.getFullName() != null ? assignedUser.getFullName() : assignedUser.getUsername());
        }
        if (subTask.getCreatorId() != null && subTask.getCreatorName() == null) {
            UsersPojo creatorUser = userMapper.selectById(subTask.getCreatorId());
            if (creatorUser != null) subTask.setCreatorName(creatorUser.getFullName() != null ? creatorUser.getFullName() : creatorUser.getUsername());
        }
    }

    // --- 修改：checkAndCompleteParentTask (现在重命名为 checkAndTriggerParentCompletion) ---
    /**
     * 递归检查并自动完成父级（主任务或子任务）
     * @param completedTask 刚刚被设置为 "COMPLETED" 状态的子任务
     */
    @Transactional
    protected void checkAndTriggerParentCompletion(SubTaskPojo completedTask,String location) {

        // 1. 检查父子任务 (Parent SubTask)
        if (completedTask.getParentSubTaskId() != null) {
            Integer parentSubId = completedTask.getParentSubTaskId();

            // 检查同级任务是否都已完成
            QueryWrapper<SubTaskPojo> siblingQuery = new QueryWrapper<>();
            siblingQuery.eq("parent_sub_task_id", parentSubId);
            siblingQuery.ne("status", "COMPLETED"); // 查找未完成的同级
            siblingQuery.last("LIMIT 1");

            Long uncompletedSiblings = subTaskMapper.selectCount(siblingQuery);

            // 如果没有未完成的同级任务 (uncompletedSiblings == 0)
            if (uncompletedSiblings == 0) {
                // 将父子任务标记为完成
                SubTaskPojo parentSubTask = subTaskMapper.selectById(parentSubId);
                if (parentSubTask != null && !"COMPLETED".equals(parentSubTask.getStatus())) {
                    System.out.println("子任务 #" + parentSubId + " 因所有下级任务完成而自动完成。");
                    parentSubTask.setStatus("COMPLETED");
                    parentSubTask.setCompletedAt(LocalDateTime.now());
                    parentSubTask.setUpdatedAt(LocalDateTime.now());
                    // 假设父子任务的完成不需要交付物或审批评论
                    // parentSubTask.setApprovalComment("所有子任务已自动完成");
                    subTaskMapper.updateById(parentSubTask);

                    UsersPojo assignee = userMapper.selectById(parentSubTask.getAssignedToId());
                    logService.logSubTaskActivity(assignee, parentSubTask, "SUBTASK_COMPLETED_AUTO", "（自动）完成了",location);

                    // --- 递归调用 ---
                    // 检查这个刚刚完成的父子任务是否会触发它的父级完成
                    checkAndTriggerParentCompletion(parentSubTask,location);
                }
            }

            // 2. 检查父主任务 (Parent Task)
        } else if (completedTask.getParentTaskId() != null) {
            Integer parentTaskId = completedTask.getParentTaskId();

            // 检查该主任务下的所有*直接*子任务是否都已完成
            // (注意：这里的假设是，只有当所有第一层子任务都 COMPLETED 时，主任务才 COMPLETED)
            // (如果要求所有孙任务也必须完成，这里的查询逻辑需要更复杂)
            QueryWrapper<SubTaskPojo> taskChildrenQuery = new QueryWrapper<>();
            taskChildrenQuery.eq("parent_task_id", parentTaskId);
            taskChildrenQuery.isNull("parent_sub_task_id"); // 只查第一层
            taskChildrenQuery.ne("status", "COMPLETED");
            taskChildrenQuery.last("LIMIT 1");

            Long uncompletedChildren = subTaskMapper.selectCount(taskChildrenQuery);

            if (uncompletedChildren == 0) {
                // 将父主任务标记为完成
                TaskPojo parentTask = taskMapper.selectById(parentTaskId);
                if (parentTask != null && !"COMPLETED".equals(parentTask.getStatus())) {
                    System.out.println("主任务 #" + parentTaskId + " 因所有子任务完成而自动完成。");
                    parentTask.setStatus("COMPLETED");
                    parentTask.setUpdatedAt(LocalDateTime.now());
                    taskMapper.updateById(parentTask);
                }
            }

            // 3. 个人任务 (无父级)
        } else {
            // 个人任务没有父级，递归结束
            return;
        }
    }
}