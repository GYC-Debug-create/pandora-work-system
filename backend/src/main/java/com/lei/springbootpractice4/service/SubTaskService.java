package com.lei.springbootpractice4.service;

import com.lei.springbootpractice4.pojo.SubTaskPojo;
import com.lei.springbootpractice4.pojo.UsersPojo;

import java.util.List;

public interface SubTaskService {
    SubTaskPojo createSubTask(SubTaskPojo subTask, String creatorId,String location);
    SubTaskPojo submitDeliverable(Integer subTaskId, String deliverableContentJson, String currentUserId,String location);
    SubTaskPojo reviewSubTask(Integer subTaskId, boolean approved, String comment, String reviewerId,String location);
    // 后续可以添加 getSubTaskById 等方法
    SubTaskPojo completeSubTask(Integer subTaskId, String currentUserId,String location);
    SubTaskPojo getSubTaskById(Integer subTaskId, UsersPojo currentUser);
    /**
     * 获取个人十大公司派发任务 (最新的10个未逾期的、创建人不是自己但负责人是自己的子任务)
     * @param currentUserId 当前用户ID
     * @return 子任务列表
     */
    List<SubTaskPojo> getTop10CompanyAssignedSubTasks(String currentUserId);

    /**
     * 获取个人十大重要事项 (最新的10个未逾期的、创建人是自己的子任务)
     * @param currentUserId 当前用户ID
     * @return 子任务列表
     */
    List<SubTaskPojo> getTop10PersonalCreatedSubTasks(String currentUserId);
}