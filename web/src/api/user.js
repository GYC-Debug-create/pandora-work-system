// Mock数据
const mockUsers = [
  { id: 1, name: '张三', email: 'user-8ab0c230@example.invalid', role: '创始人', status: '活跃', createTime: '2024-01-15' },
  { id: 2, name: '李四', email: 'user-2535fa39@example.invalid', role: '部门老总', status: '活跃', createTime: '2024-02-20' },
  { id: 3, name: '王五', email: 'user-7a76a4ca@example.invalid', role: '部门老总', status: '活跃', createTime: '2024-03-10' },
  { id: 4, name: '赵六', email: 'user-4e35f2e7@example.invalid', role: '团队长', status: '活跃', createTime: '2024-04-05' },
  { id: 5, name: '钱七', email: 'user-0070b2a3@example.invalid', role: '普通用户', status: '活跃', createTime: '2024-05-12' },
  { id: 6, name: '孙八', email: 'user-fe78a7f7@example.invalid', role: '普通用户', status: '禁用', createTime: '2024-06-18' },
  { id: 7, name: '周九', email: 'user-aaca077a@example.invalid', role: '团队长', status: '活跃', createTime: '2024-07-22' },
  { id: 8, name: '吴十', email: 'user-9ea892ed@example.invalid', role: '普通用户', status: '活跃', createTime: '2024-08-30' }
]

/**
 * 登录
 * @param {Object} data 登录信息
 * @returns {Promise}
 */
export function login(data) {
  return Promise.resolve({
    data: {
      token: '<REDACTED>',
      user: { name: 'admin', role: '系统管理员' }
    }
  })
}

/**
 * 获取用户列表
 * @param {Object} params 查询参数
 * @returns {Promise}
 */
export function getUserList(params = {}) {
  return new Promise((resolve) => {
    setTimeout(() => {
      // 默认参数，避免解构undefined导致错误
      const { pageNum = 1, pageSize = 10, name, email, role } = params
      
      let filteredUsers = [...mockUsers]
      
      // 应用筛选条件
      if (name) {
        filteredUsers = filteredUsers.filter(user => 
          user.name.toLowerCase().includes(name.toLowerCase())
        )
      }
      if (email) {
        filteredUsers = filteredUsers.filter(user => 
          user.email.toLowerCase().includes(email.toLowerCase())
        )
      }
      if (role) {
        filteredUsers = filteredUsers.filter(user => user.role === role)
      }
      
      // 分页处理
      const total = filteredUsers.length
      const start = (pageNum - 1) * pageSize
      const end = start + pageSize
      const paginatedUsers = filteredUsers.slice(start, end)
      
      resolve({
        data: paginatedUsers,
        total: total,
        pageNum: pageNum,
        pageSize: pageSize
      })
    }, 300) // 模拟网络延迟
  })
}

/**
 * 获取用户详情
 * @param {number} id 用户ID
 * @returns {Promise}
 */
export function getUserDetail(id) {
  return new Promise((resolve, reject) => {
    setTimeout(() => {
      const user = mockUsers.find(u => u.id === parseInt(id))
      if (user) {
        resolve({ data: user })
      } else {
        reject(new Error('用户不存在'))
      }
    }, 200)
  })
}

/**
 * 创建用户
 * @param {Object} data 用户数据
 * @returns {Promise}
 */
export function createUser(data) {
  return new Promise((resolve) => {
    setTimeout(() => {
      const newUser = {
        ...data,
        id: Math.max(...mockUsers.map(u => u.id)) + 1,
        createTime: new Date().toISOString().split('T')[0]
      }
      mockUsers.push(newUser)
      resolve({ data: newUser })
    }, 300)
  })
}

/**
 * 更新用户
 * @param {number} id 用户ID
 * @param {Object} data 用户数据
 * @returns {Promise}
 */
export function updateUser(id, data) {
  return new Promise((resolve, reject) => {
    setTimeout(() => {
      const index = mockUsers.findIndex(u => u.id === parseInt(id))
      if (index !== -1) {
        mockUsers[index] = { ...mockUsers[index], ...data }
        resolve({ data: mockUsers[index] })
      } else {
        reject(new Error('用户不存在'))
      }
    }, 300)
  })
}

/**
 * 删除用户
 * @param {number} id 用户ID
 * @returns {Promise}
 */
export function deleteUser(id) {
  return new Promise((resolve, reject) => {
    setTimeout(() => {
      const index = mockUsers.findIndex(u => u.id === parseInt(id))
      if (index !== -1) {
        mockUsers.splice(index, 1)
        resolve({ data: { message: '删除成功' } })
      } else {
        reject(new Error('用户不存在'))
      }
    }, 300)
  })
}
