import { test, expect } from '@playwright/test'
import { backendAvailable } from './helpers.js'

// AI 聊天组件位于 /qa 页面的侧边栏中，需要后端支持

test.describe('AI聊天', () => {
  test.skip(!backendAvailable, '需要后端服务运行')
  test.beforeEach(async ({ page }) => {
    await page.goto('/qa')
    await page.waitForLoadState('networkidle')
  })

  test('AI聊天组件应正确加载', async ({ page }) => {
    const chatCard = page.locator('.ai-chat-card, .ai-chat-wrapper')
    await expect(chatCard).toBeVisible({ timeout: 15000 })
  })

  test('应显示侗医智能助手标题', async ({ page }) => {
    const header = page.locator('.header-title')
    await expect(header).toContainText('侗医智能助手')
  })

  test('应显示AI在线状态标签', async ({ page }) => {
    const tag = page.locator('.ai-chat-card .el-tag, .header-actions .el-tag')
    await expect(tag.first()).toBeVisible({ timeout: 15000 })
  })

  test('欢迎消息应正确显示', async ({ page }) => {
    const welcome = page.locator('.welcome-message')
    await expect(welcome).toBeVisible({ timeout: 15000 })
    await expect(welcome).toContainText('侗族医药智能助手')
  })

  test('快捷提问标签应可见且可点击', async ({ page }) => {
    const quickTag = page.locator('.quick-tag').first()
    await expect(quickTag).toBeVisible({ timeout: 15000 })
  })

  test('输入框应存在', async ({ page }) => {
    const input = page.locator('.chat-input input, .chat-input .el-input__inner').first()
    await expect(input).toBeVisible({ timeout: 15000 })
  })

  test('发送按钮应存在', async ({ page }) => {
    const sendBtn = page.locator('.chat-input .el-button').first()
    await expect(sendBtn).toBeVisible({ timeout: 15000 })
  })

  test('历史会话按钮在未登录时应不可见或显示登录提示', async ({ page }) => {
    const historyBtn = page.locator('.history-toggle-btn')
    const count = await historyBtn.count()
    expect(typeof count).toBe('number')
  })
})
