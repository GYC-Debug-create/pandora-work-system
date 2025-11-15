/**
 * 公司十大事项相关 API
 */
import request from '@/utils/request'

/**
 * 获取公司事项列表
 * @param {Object} params - 查询参数
 * @returns {Promise}
 */
export function getCompanyMatters(params = {}) {
  return request({
    url: '/api/company-matters',
    method: 'get',
    params
  })
}

/**
 * 创建公司事项
 * @param {Object} data - 事项数据 { title, content }
 * @returns {Promise}
 */
export function createCompanyMatter(data) {
  return request({
    url: '/api/company-matters',
    method: 'post',
    data
  })
}

/**
 * 更新公司事项
 * @param {number} id - 事项ID
 * @param {Object} data - 事项数据 { title, content }
 * @returns {Promise}
 */
export function updateCompanyMatter(id, data) {
  return request({
    url: `/api/company-matters/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除公司事项
 * @param {number} id - 事项ID
 * @returns {Promise}
 */
export function deleteCompanyMatter(id) {
  return request({
    url: `/api/company-matters/${id}`,
    method: 'delete'
  })
}

