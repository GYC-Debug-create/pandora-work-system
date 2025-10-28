// web/src/api/role.js
import request from '@/utils/request'

/**
 * 获取所有角色列表
 * @returns {Promise} 期望返回 RolePojo 数组 [{ roleId: 1, roleName: 'ADMIN' }, ...]
 */
export function getRoles() {
    return request({
        url: '/roles', // 对应后端的 @GetMapping("/api/roles")
        method: 'get'
    })
}