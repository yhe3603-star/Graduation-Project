/**
 * @file 用户相关API
 */
import request from '@/utils/request'

/** 用户登录 */
export function login(data) {
  return request.post('/user/login', data)
}

/** 用户注册 */
export function register(data) {
  return request.post('/user/register', data)
}

/** 获取当前用户信息 */
export function getUserInfo() {
  return request.get('/user/me', { skipAuthRefresh: true })
}

/** 验证令牌有效性 */
export function validateToken() {
  return request.get('/user/validate', { skipAuthRefresh: true })
}

/** 退出登录 */
export function logout() {
  return request.post('/user/logout', {}, { skipAuthRefresh: true })
}

/** 修改密码 */
export function changePassword(data) {
  return request.post('/user/change-password', data)
}

/** 获取验证码 */
export function getCaptcha() {
  return request.get('/captcha')
}
