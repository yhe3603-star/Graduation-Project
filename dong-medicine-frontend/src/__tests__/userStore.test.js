import { describe, it, expect, beforeEach, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'

vi.mock('@/utils/request', () => ({
  default: {
    get: vi.fn(),
    post: vi.fn()
  }
}))

import request from '@/utils/request'
import { useUserStore } from '@/stores/user'

function createJwtToken(payload, expOffsetMs = 3600000) {
  const header = btoa(JSON.stringify({ alg: 'HS256', typ: 'JWT' }))
  const exp = Math.floor((Date.now() + expOffsetMs) / 1000)
  const fullPayload = { ...payload, exp }
  const body = btoa(JSON.stringify(fullPayload))
  return `${header}.${body}.signature`
}

describe('useUserStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    localStorage.clear()
    vi.clearAllMocks()
  })

  describe('setAuth', () => {
    it('应设置认证数据', () => {
      const store = useUserStore()
      store.setAuth({ token: 'test-token', id: '1', username: 'admin', role: 'admin' })
      expect(store.token).toBe('test-token')
      expect(store.userId).toBe('1')
      expect(store.username).toBe('admin')
      expect(store.role).toBe('admin')
    })

    it('应持久化到localStorage', () => {
      const store = useUserStore()
      store.setAuth({ token: 'test-token', id: '1', username: 'admin', role: 'admin' })
      expect(localStorage.getItem('token')).toBe('test-token')
      expect(localStorage.getItem('userId')).toBe('1')
      expect(localStorage.getItem('userName')).toBe('admin')
      expect(localStorage.getItem('role')).toBe('admin')
    })

    it('缺少role应默认为user', () => {
      const store = useUserStore()
      store.setAuth({ token: 'test-token', id: '1', username: 'test' })
      expect(store.role).toBe('user')
    })
  })

  describe('clearAuth', () => {
    it('应清除所有认证数据', () => {
      const store = useUserStore()
      store.setAuth({ token: 'test-token', id: '1', username: 'admin', role: 'admin' })
      store.clearAuth()
      expect(store.token).toBe('')
      expect(store.userId).toBe('')
      expect(store.username).toBe('')
      expect(store.role).toBe('')
      expect(store.userInfo).toBeNull()
    })

    it('应清除localStorage', () => {
      const store = useUserStore()
      store.setAuth({ token: 'test-token', id: '1', username: 'admin', role: 'admin' })
      store.clearAuth()
      expect(localStorage.getItem('token')).toBeNull()
    })
  })

  describe('isLoggedIn', () => {
    it('无token应返回false', () => {
      const store = useUserStore()
      expect(store.isLoggedIn).toBe(false)
    })

    it('有效JWT token应返回true', () => {
      const store = useUserStore()
      const token = createJwtToken({ sub: '1' }, 3600000)
      store.setAuth({ token, id: '1', username: 'test', role: 'user' })
      expect(store.isLoggedIn).toBe(true)
    })

    it('过期JWT token应返回false', () => {
      const store = useUserStore()
      const token = createJwtToken({ sub: '1' }, -1000)
      store.setAuth({ token, id: '1', username: 'test', role: 'user' })
      expect(store.isLoggedIn).toBe(false)
    })
  })

  describe('isAdmin', () => {
    it('admin角色应返回true', () => {
      const store = useUserStore()
      const token = createJwtToken({ sub: '1' }, 3600000)
      store.setAuth({ token, id: '1', username: 'admin', role: 'admin' })
      expect(store.isAdmin).toBe(true)
    })

    it('user角色应返回false', () => {
      const store = useUserStore()
      const token = createJwtToken({ sub: '1' }, 3600000)
      store.setAuth({ token, id: '1', username: 'test', role: 'user' })
      expect(store.isAdmin).toBe(false)
    })

    it('无token应返回false', () => {
      const store = useUserStore()
      expect(store.isAdmin).toBe(false)
    })

    it('大小写不敏感', () => {
      const store = useUserStore()
      const token = createJwtToken({ sub: '1' }, 3600000)
      store.setAuth({ token, id: '1', username: 'admin', role: 'Admin' })
      expect(store.isAdmin).toBe(true)
    })
  })

  describe('login', () => {
    it('登录成功应设置认证数据', async () => {
      request.post.mockResolvedValue({
        code: 200,
        data: { token: 'new-token', id: '1', username: 'test', role: 'user' }
      })
      const store = useUserStore()
      const result = await store.login({ username: 'test', password: 'Test1234' })
      expect(result.success).toBe(true)
      expect(store.token).toBe('new-token')
    })

    it('登录失败应返回错误信息', async () => {
      request.post.mockResolvedValue({ code: 500, msg: '密码错误' })
      const store = useUserStore()
      const result = await store.login({ username: 'test', password: 'wrong' })
      expect(result.success).toBe(false)
      expect(result.message).toBe('密码错误')
    })

    it('网络异常应返回错误信息', async () => {
      request.post.mockRejectedValue(new Error('Network Error'))
      const store = useUserStore()
      const result = await store.login({ username: 'test', password: 'Test1234' })
      expect(result.success).toBe(false)
    })
  })

  describe('logout', () => {
    it('退出后应清除认证数据', async () => {
      request.post.mockResolvedValue({ code: 200 })
      const store = useUserStore()
      store.setAuth({ token: 'test-token', id: '1', username: 'test', role: 'user' })
      await store.logout()
      expect(store.token).toBe('')
    })

    it('请求失败也应清除认证数据', async () => {
      request.post.mockRejectedValue(new Error('fail'))
      const store = useUserStore()
      store.setAuth({ token: 'test-token', id: '1', username: 'test', role: 'user' })
      await store.logout()
      expect(store.token).toBe('')
    })
  })

  describe('changePassword', () => {
    it('修改成功应清除认证数据', async () => {
      request.post.mockResolvedValue({ code: 200, data: '密码修改成功' })
      const store = useUserStore()
      store.setAuth({ token: 'test-token', id: '1', username: 'test', role: 'user' })
      const result = await store.changePassword({ oldPassword: 'Old1234', newPassword: 'New1234' })
      expect(result.success).toBe(true)
      expect(store.token).toBe('')
    })

    it('修改失败应返回错误信息', async () => {
      request.post.mockResolvedValue({ code: 400, msg: '旧密码错误' })
      const store = useUserStore()
      const result = await store.changePassword({ oldPassword: 'wrong', newPassword: 'New1234' })
      expect(result.success).toBe(false)
    })
  })

  describe('validateToken', () => {
    it('有效Token应返回true并更新用户信息', async () => {
      request.get.mockResolvedValue({
        code: 200,
        data: { id: '1', username: 'test', role: 'user' }
      })
      const store = useUserStore()
      const token = createJwtToken({ sub: '1' }, 3600000)
      store.setAuth({ token, id: '1', username: 'test', role: 'user' })
      const result = await store.validateToken()
      expect(result).toBe(true)
    })

    it('无效Token应返回false并清除认证', async () => {
      request.get.mockResolvedValue({ code: 401 })
      const store = useUserStore()
      const token = createJwtToken({ sub: '1' }, 3600000)
      store.setAuth({ token, id: '1', username: 'test', role: 'user' })
      const result = await store.validateToken()
      expect(result).toBe(false)
    })

    it('无token应返回false', async () => {
      const store = useUserStore()
      const result = await store.validateToken()
      expect(result).toBe(false)
    })
  })

  describe('fetchUserInfo', () => {
    it('成功应返回用户信息', async () => {
      request.get.mockResolvedValue({
        code: 200,
        data: { id: '1', username: 'test', role: 'user' }
      })
      const store = useUserStore()
      const token = createJwtToken({ sub: '1' }, 3600000)
      store.setAuth({ token, id: '1', username: 'test', role: 'user' })
      const result = await store.fetchUserInfo()
      expect(result).toEqual({ id: '1', username: 'test', role: 'user' })
    })

    it('无token应返回null', async () => {
      const store = useUserStore()
      const result = await store.fetchUserInfo()
      expect(result).toBeNull()
    })
  })

  describe('回归测试', () => {
    it('Bug: JWT解码失败不应崩溃', () => {
      const store = useUserStore()
      store.setAuth({ token: 'invalid.jwt.token', id: '1', username: 'test', role: 'user' })
      expect(() => store.isLoggedIn).not.toThrow()
    })

    it('Bug: localStorage不可用时应降级', () => {
      const original = localStorage.setItem
      localStorage.setItem = () => { throw new Error('QuotaExceeded') }
      const store = useUserStore()
      expect(() => store.setAuth({ token: 'test', id: '1', username: 'test', role: 'user' })).not.toThrow()
      localStorage.setItem = original
    })

    it('Bug: auth-expired事件应清除认证', () => {
      const store = useUserStore()
      store.setAuth({ token: 'test-token', id: '1', username: 'test', role: 'user' })
      store.initialize()
      window.dispatchEvent(new CustomEvent('auth-expired'))
      expect(store.token).toBe('')
      store.destroy()
    })

    it('Bug: 修改密码后应强制重新登录', async () => {
      request.post.mockResolvedValue({ code: 200, data: '密码修改成功' })
      const store = useUserStore()
      store.setAuth({ token: 'test-token', id: '1', username: 'test', role: 'user' })
      await store.changePassword({ oldPassword: 'Old1234', newPassword: 'New1234' })
      expect(store.isLoggedIn).toBe(false)
    })
  })
})
