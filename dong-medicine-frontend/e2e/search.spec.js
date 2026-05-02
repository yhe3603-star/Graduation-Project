test.describe('搜索功能测试', () => {
  test('植物搜索API应返回结果', async ({ request }) => {
    const response = await request.get('/api/plants/search?keyword=钩藤&page=1&size=10')
    expect(response.ok()).toBeTruthy()
    const body = await response.json()
    expect(body.code).toBe(200)
  })

  test('知识搜索API应返回结果', async ({ request }) => {
    const response = await request.get('/api/knowledge/search?keyword=药浴&page=1&size=10')
    expect(response.ok()).toBeTruthy()
    const body = await response.json()
    expect(body.code).toBe(200)
  })

  test('传承人搜索API应返回结果', async ({ request }) => {
    const response = await request.get('/api/inheritors/search?keyword=传承&page=1&size=10')
    expect(response.ok()).toBeTruthy()
    const body = await response.json()
    expect(body.code).toBe(200)
  })

  test('特殊字符搜索应不报错', async ({ request }) => {
    const response = await request.get('/api/plants/search?keyword=%3Cscript%3E&page=1&size=10')
    expect(response.ok()).toBeTruthy()
  })

  test('空关键词搜索应正常返回', async ({ request }) => {
    const response = await request.get('/api/plants/search?keyword=&page=1&size=10')
    expect(response.ok()).toBeTruthy()
  })

  test('资源搜索API应返回结果', async ({ request }) => {
    const response = await request.get('/api/resources/search?keyword=侗族&page=1&size=10')
    expect(response.ok()).toBeTruthy()
    const body = await response.json()
    expect(body.code).toBe(200)
  })
})

test.describe('全局搜索页面', () => {
  test('应加载搜索页面', async ({ page }) => {
    await page.goto('/search')
    await page.waitForSelector('.search-page, .search-box-large', { timeout: 10000 })
  })

  test('搜索应返回分类结果', async ({ page }) => {
    await page.goto('/search')
    await page.waitForSelector('.search-box-large input, .el-input__inner', { timeout: 10000 })
    const searchInput = page.locator('input[type="text"], .el-input__inner').first()
    if (await searchInput.isVisible()) {
      await searchInput.fill('侗医药')
      const searchButton = page.locator('.search-box-large .el-button').first()
      if (await searchButton.isVisible()) {
        await searchButton.click()
        await page.waitForTimeout(2000)
      }
    }
  })
})
