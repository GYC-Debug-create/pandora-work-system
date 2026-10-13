package com.lei.springbootpractice4.config;

/**
 * 角色相关常量，对应 roles 表的种子数据 (data.sql)。
 * 管理员判断统一走这里，避免在代码里散落魔法数字 1。
 */
public final class RoleConstants {

    private RoleConstants() {
    }

    /** 管理员角色 ID (roles.role_id) */
    public static final int ADMIN_ROLE_ID = 1;

    /** 管理员角色名 (roles.role_name) */
    public static final String ADMIN_ROLE_NAME = "ADMIN";

    /** Spring Security 中的权限名：管理员 */
    public static final String AUTHORITY_ADMIN = "ROLE_ADMIN";

    /** Spring Security 中的权限名：普通已登录用户 */
    public static final String AUTHORITY_USER = "ROLE_USER";

    public static boolean isAdmin(Integer roleId) {
        return roleId != null && roleId == ADMIN_ROLE_ID;
    }
}
