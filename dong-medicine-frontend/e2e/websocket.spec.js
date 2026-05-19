import { test, expect } from '@playwright/test'

test.describe('WebSocket聊天测试', () => {
  test('WebSocket连接应能建立', async ({ page }) => {
    const wsPromise = page.waitForEvent('websocket', { timeout: 15000 })
    await page.goto('/')
    await page.waitForLoadState('networkidle')

    const chatButton = page.locator('.ai-chat-trigger, .chat-fab, [aria-label="AI助手"]').first()
    await expect(chatButton).toBeVisible({ timeout: 10000 })
    await chatButton.click()

    const ws = await wsPromise
    expect(ws.url()).toBeTruthy()
  })
})
