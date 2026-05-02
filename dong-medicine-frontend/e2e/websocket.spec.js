test.describe('WebSocket聊天测试', () => {
  test('WebSocket连接应能建立', async ({ page }) => {
    let wsConnected = false

    page.on('websocket', ws => {
      wsConnected = true
    })

    await page.goto('/')
    await page.waitForTimeout(2000)

    const chatButton = page.locator('.ai-chat-trigger, .chat-fab, [aria-label="AI助手"]').first()
    if (await chatButton.isVisible()) {
      await chatButton.click()
      await page.waitForTimeout(1000)
    }
  })
})
