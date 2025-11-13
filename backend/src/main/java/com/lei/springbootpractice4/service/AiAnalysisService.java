package com.lei.springbootpractice4.service;

import com.lei.springbootpractice4.pojo.UsersPojo;
import java.util.Map;

public interface AiAnalysisService {

    /**
     * 获取用于 AI 分析的用户数据（用户信息和最近12条子任务）
     * @param targetUserId 要查询的目标用户ID
     * @param currentUser  执行操作的当前用户（用于权限校验）
     * @return 组装好的 Map，可直接序列化为 JSON
     */
    Map<String, Object> getAiAnalysisData(String targetUserId, UsersPojo currentUser);
}