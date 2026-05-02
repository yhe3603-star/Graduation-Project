test.describe('响应式布局', () => {
  test('移动端首页应正常显示', async ({ page }) => {
    await page.setViewportSize({ width: 375, height: 812 })
    await page.goto('/')
    await page.waitForTimeout(2000)
    const body = page.locator('body')
    await expect(body).toBeVisible()
  })

  test('平板端首页应正常显示', async ({ page }) => {
    await page.setViewportSize({ width: 768, height: 1024 })
    await page.goto('/')
    await page.waitForTimeout(2000)
    const body = page.locator('body')
    await expect(body).toBeVisible()
  })

  test('桌面端首页应正常显示', async ({ page }) => {
    await page.setViewportSize({ width: 1920, height: 1080 })
    await page.goto('/')
    await page.waitForTimeout(2000)
    const body = page.locator('body')
    await expect(body).toBeVisible()
  })
})
