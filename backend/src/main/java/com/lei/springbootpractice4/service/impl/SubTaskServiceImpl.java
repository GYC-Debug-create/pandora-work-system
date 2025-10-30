package com.lei.springbootpractice4.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lei.springbootpractice4.mapper.SubTaskMapper;
import com.lei.springbootpractice4.mapper.TaskMapper; // 需要 TaskMapper 校验 parentTaskId
import com.lei.springbootpractice4.mapper.UserMapper; // 需要 UserMapper 校验 assignedToId
import com.lei.springbootpractice4.pojo.SubTaskPojo;
import com.lei.springbootpractice4.pojo.TaskPojo; // 需要 TaskPojo
import com.lei.springbootpractice4.service.SubTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // 引入事务

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
public class SubTaskServiceImpl implements SubTaskService {

    @Autowired
    private SubTaskMapper subTaskMapper;

    @Autowired
    private TaskMapper taskMapper; // 注入 TaskMapper

    @Autowired
    private UserMapper userMapper; // 注入 UserMapper

    @Override
    @Transactional
    public SubTaskPojo completeSubTask(Integer subTaskId, String currentUserId) {
        SubTaskPojo subTask = subTaskMapper.selectById(subTaskId);
        if (subTask == null) {
            throw new IllegalArgumentException("子任务不存在");
        }

        // --- 校验是否为个人任务 ---
        boolean isPersonalTask = subTask.getParentTaskId() == null && subTask.getParentSubTaskId() == null;

        // --- 权限与规则校验 ---
        if (!Objects.equals(currentUserId, subTask.getAssignedToId())) {
            throw new SecurityException("只有任务负责人才能将任务标记为完成");
        }
        if (!isPersonalTask) {
            // 非个人任务不允许通过此接口直接完成 (必须走审批流程)
            throw new IllegalStateException("只有个人创建的任务才能直接标记为完成，分解的任务需要提交审批");
        }

        // --- 状态校验 ---
        String currentStatus = subTask.getStatus();
        // 允许从 TODO, IN_PROGRESS, REJECTED 状态完成，不允许重复完成
        if ("COMPLETED".equals(currentStatus)) {
            throw new IllegalStateException("任务已经是完成状态");
        }
        if ("PENDING_APPROVAL".equals(currentStatus)) {
            // 个人任务理论上不应该进入 PENDING_APPROVAL 状态，但也处理一下
            System.out.println("警告：个人任务状态为 PENDING_APPROVAL，现在直接标记为完成。");
        }
        // 可以根据需要添加其他不允许完成的状态检查 (例如 CANCELLED)


        // --- 更新状态和完成时间 ---
        subTask.setStatus("COMPLETED");
        subTask.setCompletedAt(LocalDateTime.now());
        subTask.setUpdatedAt(LocalDateTime.now()); // 更新时间

        subTaskMapper.updateById(subTask);

        // 填充名字后返回

        return subTask;
    }

    @Override
    @Transactional
    public SubTaskPojo createSubTask(SubTaskPojo subTask, String creatorId) {
        subTask.setCreatorId(creatorId);
        subTask.setStatus("TODO");
        subTask.setCreatedAt(LocalDateTime.now());
        subTask.setUpdatedAt(LocalDateTime.now());

        Integer parentTaskId = subTask.getParentTaskId();
        Integer parentSubTaskId = subTask.getParentSubTaskId();
        String assignedToId = subTask.getAssignedToId();
        LocalDateTime startTime = subTask.getStartTime(); // 获取开始时间
        LocalDateTime endTime = subTask.getEndTime();     // 获取结束时间

        if (parentTaskId != null && parentSubTaskId != null) {
            throw new IllegalArgumentException("不能同时指定父主任务ID和父子任务ID");
        }

        // --- 设置 assignedToId (如果需要根据场景判断) ---
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
            } else { // 二次分解
                SubTaskPojo parentSubTask = subTaskMapper.selectById(parentSubTaskId);
                if (parentSubTask == null) throw new IllegalArgumentException("无效的父子任务ID: " + parentSubTaskId);
                if (!Objects.equals(creatorId, parentSubTask.getAssignedToId())) throw new SecurityException("只有父子任务的负责人才能进行二次分解");
                if (!Objects.equals(assignedToId, creatorId)) throw new IllegalArgumentException("二次分解的任务只能分配给自己");
                subTask.setParentTaskId(parentSubTask.getParentTaskId());
            }
        } else { // 个人任务
            if (assignedToId == null || assignedToId.isBlank()) {
                subTask.setAssignedToId(creatorId);
                assignedToId = creatorId; // 更新 assignedToId 变量用于后续检查
            } else if (!Objects.equals(assignedToId, creatorId)) {
                throw new IllegalArgumentException("个人任务的负责人只能是创建者本人");
            }
        }
        // --- assignedToId 设置完成 ---


        // --- 时间冲突检查 ---
        if (startTime != null && endTime != null) {
            // 1. 确保 startTime 在 endTime 之前
            if (startTime.isAfter(endTime)) {
                throw new IllegalArgumentException("子任务的开始时间不能晚于结束时间");
            }

            // 2. 查询该负责人已存在的、时间可能冲突的、非最终状态的子任务
            QueryWrapper<SubTaskPojo> conflictCheckWrapper = new QueryWrapper<>();
            conflictCheckWrapper
                    .eq("assigned_to_id", assignedToId)
                    .ne("status", "COMPLETED")
                    .ne("status", "CANCELLED")
                    // --- 新增排除条件 ---
                    .ne(parentSubTaskId != null, "sub_task_id", parentSubTaskId) // 如果是二次分解，排除父子任务本身
                    // --- 新增结束 ---
                    .and(wrapper -> wrapper
                            .lt("start_time", endTime)
                            .gt("end_time", startTime));

            List<SubTaskPojo> conflictingTasks = subTaskMapper.selectList(conflictCheckWrapper);
            // 3. 如果找到冲突的任务，则抛出异常
            if (!conflictingTasks.isEmpty()) {
                // 可以构建更详细的错误信息，比如冲突的任务名称和时间段
                SubTaskPojo conflict = conflictingTasks.get(0); // 取第一个冲突的任务
                String errorMessage = String.format(
                        "时间冲突：与任务 '%s' (%s - %s) 时间重叠。",
                        conflict.getSubTaskName(),
                        conflict.getStartTime(),
                        conflict.getEndTime()
                );
                // 使用 IllegalStateException 或自定义异常
                throw new IllegalStateException(errorMessage);
            }
        } else {
            // 如果业务要求必须有开始/结束时间，可以在这里抛出异常
            // throw new IllegalArgumentException("子任务必须指定开始和结束时间");
            System.out.println("警告: 子任务未指定开始或结束时间，跳过时间冲突检查。"); // 或者记录日志
        }
        // --- 时间冲突检查结束 ---


        subTaskMapper.insert(subTask);
        return subTask;
    }


    @Override
    @Transactional
    public SubTaskPojo submitDeliverable(Integer subTaskId, String deliverableContentJson, String currentUserId) {
        SubTaskPojo subTask = subTaskMapper.selectById(subTaskId);
        if (subTask == null) {
            throw new IllegalArgumentException("子任务不存在");
        }

        // 权限校验：只有任务负责人才能提交
        if (!Objects.equals(currentUserId, subTask.getAssignedToId())) {
            throw new SecurityException("只有子任务的负责人才能提交交付物");
        }

        // 状态校验：只有 TODO 或 IN_PROGRESS 或 REJECTED 状态才能提交
        String currentStatus = subTask.getStatus();
        if (!("TODO".equals(currentStatus) || "IN_PROGRESS".equals(currentStatus) || "REJECTED".equals(currentStatus))) {
            throw new IllegalStateException("当前任务状态 (" + currentStatus + ") 不允许提交交付物");
        }

        // 更新交付物内容和状态
        subTask.setDeliverableContent(deliverableContentJson);
        subTask.setStatus("PENDING_APPROVAL");
        subTask.setUpdatedAt(LocalDateTime.now()); // 更新时间
        // completed_at 在审批通过时设置

        subTaskMapper.updateById(subTask);
        return subTask;
    }

    @Override
    @Transactional
    public SubTaskPojo reviewSubTask(Integer subTaskId, boolean approved, String comment, String reviewerId) {
        SubTaskPojo subTask = subTaskMapper.selectById(subTaskId);
        if (subTask == null) {
            throw new IllegalArgumentException("子任务不存在");
        }

        // --- 权限校验 (简化版) ---
        // 实际项目中需要更完善的逻辑：
        // 1. 检查 reviewerId 是否是父任务 (parentTaskId) 的负责人 (responsible_person_id)？
        // 2. 或者 reviewerId 是否是该员工 (assigned_to_id) 的上级？
        // 3. 或者 reviewerId 是否是管理员？
        // 暂时简化为：不允许自己审批自己的任务
        if (Objects.equals(reviewerId, subTask.getAssignedToId())) {
            throw new SecurityException("不能审批自己提交的任务");
        }
        // 简单的校验：假设只有主任务负责人才有审批权限
        if (subTask.getParentTaskId() != null) {
            TaskPojo parentTask = taskMapper.selectById(subTask.getParentTaskId());
            if (parentTask != null && !Objects.equals(reviewerId, parentTask.getResponsiblePersonId())) {
                // 可以在这里允许创建者审批，或者部门领导审批等，根据具体需求
                // 暂定：如果不是主任务负责人，不允许审批
                // throw new SecurityException("只有主任务的负责人才有权限审批");
                System.out.println("警告：审批人不是主任务负责人（简化测试，暂不强制要求）");
            }
        } else {
            // 如果没有父主任务，如何确定审批人？暂定不允许审批无父任务的提交？
            // 或者需要一个默认审批流程？
            System.out.println("警告：该子任务没有关联主任务，审批权限未明确（简化测试，暂不强制要求）");
        }
        // --- 权限校验结束 ---


        // 状态校验：只有 PENDING_APPROVAL 状态才能审批
        if (!"PENDING_APPROVAL".equals(subTask.getStatus())) {
            throw new IllegalStateException("当前任务状态不是待审批，无法执行审批操作");
        }

        if (approved) {
            subTask.setStatus("COMPLETED");
            subTask.setCompletedAt(LocalDateTime.now()); // 设置完成时间
            subTask.setApprovalComment(comment); // 通过时也可有评论
        } else {
            if (comment == null || comment.isBlank()) {
                // Controller 层已校验，这里可以省略或再次校验
                throw new IllegalArgumentException("驳回时必须填写评论");
            }
            subTask.setStatus("REJECTED");
            subTask.setApprovalComment(comment);
            subTask.setCompletedAt(null); // 驳回时清除完成时间（如果之前有的话）
        }
        subTask.setUpdatedAt(LocalDateTime.now()); // 更新时间

        subTaskMapper.updateById(subTask);

        // --- 触发主任务自动完成检查 ---
        if (approved && subTask.getParentTaskId() != null) {
            checkAndCompleteParentTask(subTask.getParentTaskId());
        }
        // --- 触发结束 ---

        // --- 填充返回信息 (可选) ---
        // 可以在这里填充 assignedToName 和 creatorName
        // ... (填充代码同 createSubTask) ...
        // --- 填充结束 ---


        return subTask;
    }

    // --- 主任务自动完成检查逻辑 ---
    private void checkAndCompleteParentTask(Integer parentTaskId) {
        if (parentTaskId == null) return;

        // 1. 检查该主任务下是否还有任何未完成的子任务 (包括所有层级)
        // 注意：这里的查询逻辑可能比较复杂，取决于你是否需要检查所有层级的子孙任务
        // 简化版本：只检查直接子任务
        QueryWrapper<SubTaskPojo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("parent_task_id", parentTaskId);
        queryWrapper.ne("status", "COMPLETED"); // 查询状态不是 COMPLETED 的
        queryWrapper.last("LIMIT 1"); // 找到一个就够了

        Long uncompletedCount = subTaskMapper.selectCount(queryWrapper);

        // 如果找不到任何未完成的子任务
        if (uncompletedCount == 0) {
            // 查询主任务
            TaskPojo parentTask = taskMapper.selectById(parentTaskId);
            // 如果主任务当前状态不是 COMPLETED，则更新它
            if (parentTask != null && !"COMPLETED".equals(parentTask.getStatus())) {
                parentTask.setStatus("COMPLETED");
                parentTask.setUpdatedAt(LocalDateTime.now());
                taskMapper.updateById(parentTask);
                System.out.println("主任务 #" + parentTaskId + " 因所有子任务完成而自动完成。");
                // TODO: 可以添加日志记录或发送通知
            }
        }
    }
}