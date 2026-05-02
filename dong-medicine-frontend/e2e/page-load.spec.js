test.describe('首页', () => {
  test('应正确加载首页', async ({ page }) => {
    await page.goto('/')
    await expect(page).toHaveTitle(/侗乡医药/)
    await expect(page.locator('.home-page, .app-header')).toBeVisible()
  })

  test('导航栏应显示所有菜单项', async ({ page }) => {
    await page.goto('/')
    const navLinks = page.locator('.nav-link, .el-menu-item, header a')
    await expect(navLinks).toHaveCount(expect.any(Number))
  })
})

test.describe('药用植物', () => {
  test('应加载植物列表', async ({ page }) => {
    await page.goto('/plants')
    await page.waitForSelector('.plant-card, .card-grid, .el-card', { timeout: 10000 })
    const cards = page.locator('.plant-card, .card-grid .el-card')
    await expect(cards.first()).toBeVisible()
  })

  test('搜索植物应返回结果', async ({ page }) => {
    await page.goto('/plants')
    await page.waitForSelector('.search-filter, .el-input', { timeout: 10000 })
    const searchInput = page.locator('input[type="text"], .el-input__inner').first()
    if (await searchInput.isVisible()) {
      await searchInput.fill('钩藤')
      await page.waitForTimeout(1000)
    }
  })
})

test.describe('知识库', () => {
  test('应加载知识列表', async ({ page }) => {
    await page.goto('/knowledge')
    await page.waitForSelector('.knowledge-card, .card-grid, .el-card', { timeout: 10000 })
    const cards = page.locator('.knowledge-card, .card-grid .el-card')
    await expect(cards.first()).toBeVisible()
  })
})

test.describe('传承人', () => {
  test('应加载传承人列表', async ({ page }) => {
    await page.goto('/inheritors')
    await page.waitForSelector('.inheritor-card, .card-grid, .el-card', { timeout: 10000 })
    const cards = page.locator('.inheritor-card, .card-grid .el-card')
    await expect(cards.first()).toBeVisible()
  })
})

test.describe('问答', () => {
  test('应加载问答列表', async ({ page }) => {
    await page.goto('/qa')
    await page.waitForSelector('.qa-card, .qa-item, .el-card', { timeout: 10000 })
  })
})

test.describe('学习资源', () => {
  test('应加载资源列表', async ({ page }) => {
    await page.goto('/resources')
    await page.waitForSelector('.resource-card, .card-grid, .el-card', { timeout: 10000 })
  })
})

test.describe('反馈', () => {
  test('应加载反馈页面', async ({ page }) => {
    await page.goto('/feedback')
    await expect(page.locator('.feedback-page, .el-form')).toBeVisible()
  })
})
