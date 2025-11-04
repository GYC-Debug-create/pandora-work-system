// web/src/api/department.js
import request from '@/utils/request'

/**
 * 获取所有部门列表
 * @returns {Promise}
 */
export function getDepartments() {
    return request({
        url: '/api/departments',
        method: 'get'
    })
    // 期望返回 DepartmentPojo 数组 [{ departmentId: 1, departmentName: '技术部' }, ...]
}