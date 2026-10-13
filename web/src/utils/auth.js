/**
 * 登录会话（token + 登录时后端返回的用户信息）的读写
 * 统一放在这里，避免 sessionStorage 的 key 散落在各个页面
 */

export const TOKEN_KEY = 'admin-token'
export const USER_INFO_KEY = 'user-info'

export function getToken() {
  return sessionStorage.getItem(TOKEN_KEY)
}

/** 获取登录时保存的用户信息；没有或解析失败时返回空对象 */
export function getUserInfo() {
  try {
    return JSON.parse(sessionStorage.getItem(USER_INFO_KEY) || '{}') || {}
  } catch (e) {
    return {}
  }
}

export function setSession(token, userInfo) {
  sessionStorage.setItem(TOKEN_KEY, token)
  sessionStorage.setItem(USER_INFO_KEY, JSON.stringify(userInfo || {}))
}

export function clearSession() {
  sessionStorage.removeItem(TOKEN_KEY)
  sessionStorage.removeItem(USER_INFO_KEY)
}
