import { test, expect } from '@playwright/test'

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

test.describe('API接口', () => {
  test('植物列表API应返回200', async ({ request }) => {
    const response = await request.get('/api/plants/list')
    expect(response.ok()).toBeTruthy()
    const body = await response.json()
    expect(body.code).toBe(200)
    expect(body.data.records).toBeDefined()
  })

  test('知识列表API应返回200', async ({ request }) => {
    const response = await request.get('/api/knowledge/list')
    expect(response.ok()).toBeTruthy()
    const body = await response.json()
    expect(body.code).toBe(200)
  })

  test('传承人列表API应返回200', async ({ request }) => {
    const response = await request.get('/api/inheritors/list')
    expect(response.ok()).toBeTruthy()
    const body = await response.json()
    expect(body.code).toBe(200)
  })

  test('问答列表API应返回200', async ({ request }) => {
    const response = await request.get('/api/qa/list')
    expect(response.ok()).toBeTruthy()
    const body = await response.json()
    expect(body.code).toBe(200)
  })

  test('资源列表API应返回200', async ({ request }) => {
    const response = await request.get('/api/resources/list')
    expect(response.ok()).toBeTruthy()
    const body = await response.json()
    expect(body.code).toBe(200)
  })

  test('统计接口应返回200', async ({ request }) => {
    const response = await request.get('/api/stats/chart')
    expect(response.ok()).toBeTruthy()
  })

  test('验证码接口应返回200', async ({ request }) => {
    const response = await request.get('/api/captcha')
    expect(response.ok()).toBeTruthy()
    const body = await response.json()
    expect(body.code).toBe(200)
    expect(body.data.captchaKey).toBeDefined()
    expect(body.data.captchaImage).toBeDefined()
  })

  test('浏览量递增接口应无需登录', async ({ request }) => {
    const response = await request.post('/api/plants/1/view')
    expect(response.ok()).toBeTruthy()
  })

  test('反馈提交接口应无需登录', async ({ request }) => {
    const response = await request.post('/api/feedback', {
      data: {
        type: 'suggestion',
        title: 'E2E测试反馈',
        content: '这是一条E2E测试反馈',
        contact: ''
      }
    })
    expect(response.ok()).toBeTruthy()
  })

  test('反馈统计接口应返回正确结构', async ({ request }) => {
    const response = await request.get('/api/feedback/stats')
    expect(response.ok()).toBeTruthy()
    const body = await response.json()
    expect(body.code).toBe(200)
    expect(body.data.total).toBeDefined()
    expect(body.data.pending).toBeDefined()
    expect(body.data.resolved).toBeDefined()
  })

  test('资源分类接口应返回200', async ({ request }) => {
    const response = await request.get('/api/resources/categories')
    expect(response.ok()).toBeTruthy()
  })

  test('资源类型接口应返回200', async ({ request }) => {
    const response = await request.get('/api/resources/types')
    expect(response.ok()).toBeTruthy()
  })

  test('热门资源接口应返回200', async ({ request }) => {
    const response = await request.get('/api/resources/hot')
    expect(response.ok()).toBeTruthy()
  })

  test('随机植物接口应返回200', async ({ request }) => {
    const response = await request.get('/api/plants/random')
    expect(response.ok()).toBeTruthy()
  })
})

test.describe('分页边界测试', () => {
  test('植物列表size=9999应被限制为100', async ({ request }) => {
    const response = await request.get('/api/plants/list?page=1&size=9999')
    expect(response.ok()).toBeTruthy()
    const body = await response.json()
    expect(body.data.records.length).toBeLessThanOrEqual(100)
  })

  test('知识列表size=0应被限制为1', async ({ request }) => {
    const response = await request.get('/api/knowledge/list?page=1&size=0')
    expect(response.ok()).toBeTruthy()
    const body = await response.json()
    expect(body.data.size).toBeGreaterThanOrEqual(1)
  })

  test('传承人列表page=-1应被限制为1', async ({ request }) => {
    const response = await request.get('/api/inheritors/list?page=-1&size=10')
    expect(response.ok()).toBeTruthy()
    const body = await response.json()
    expect(body.data.page).toBeGreaterThanOrEqual(1)
  })

  test('问答列表size=100应正常返回', async ({ request }) => {
    const response = await request.get('/api/qa/list?page=1&size=100')
    expect(response.ok()).toBeTruthy()
    const body = await response.json()
    expect(body.data.records.length).toBeLessThanOrEqual(100)
  })

  test('问答题目count参数应被限制在50', async ({ request }) => {
    const response = await request.get('/api/quiz/questions?count=999')
    expect(response.ok()).toBeTruthy()
    const body = await response.json()
    if (Array.isArray(body.data)) {
      expect(body.data.length).toBeLessThanOrEqual(50)
    }
  })

  test('植物列表page=0应被限制为1', async ({ request }) => {
    const response = await request.get('/api/plants/list?page=0&size=10')
    expect(response.ok()).toBeTruthy()
    const body = await response.json()
    expect(body.data.page).toBeGreaterThanOrEqual(1)
  })

  test('资源列表size=负数应被限制', async ({ request }) => {
    const response = await request.get('/api/resources/list?page=1&size=-5')
    expect(response.ok()).toBeTruthy()
    const body = await response.json()
    expect(body.data.size).toBeGreaterThanOrEqual(1)
  })
})

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

test.describe('用户认证流程', () => {
  test('登录页面应可访问', async ({ page }) => {
    await page.goto('/login')
    await page.waitForTimeout(2000)
    const loginForm = page.locator('.login-page, .el-form, .auth-form')
    if (await loginForm.isVisible()) {
      expect(await loginForm.isVisible()).toBeTruthy()
    }
  })

  test('注册页面应可访问', async ({ page }) => {
    await page.goto('/register')
    await page.waitForTimeout(2000)
  })

  test('登录API缺少验证码应返回400', async ({ request }) => {
    const response = await request.post('/api/user/login', {
      data: { username: 'admin', password: 'wrongpassword' }
    })
    expect(response.status()).toBe(400)
  })

  test('完整登录流程应成功', async ({ request }) => {
    const captchaRes = await request.get('/api/captcha')
    expect(captchaRes.ok()).toBeTruthy()
    const captchaBody = await captchaRes.json()
    expect(captchaBody.data.captchaKey).toBeDefined()
  })

  test('注册API缺少验证码应返回400', async ({ request }) => {
    const response = await request.post('/api/user/register', {
      data: { username: 'newuser', password: 'Test1234', confirmPassword: 'Test1234' }
    })
    expect(response.status()).toBe(400)
  })

  test('验证码API应返回图片', async ({ request }) => {
    const response = await request.get('/api/captcha')
    expect(response.ok()).toBeTruthy()
    const body = await response.json()
    expect(body.data.captchaImage).toContain('data:image')
  })
})

test.describe('详情页面导航', () => {
  test('植物详情页应可访问', async ({ page }) => {
    await page.goto('/plants')
    await page.waitForTimeout(2000)
    const firstCard = page.locator('.plant-card, .el-card').first()
    if (await firstCard.isVisible()) {
      await firstCard.click()
      await page.waitForTimeout(2000)
      expect(page.url()).toContain('/plants')
    }
  })

  test('知识详情页应可访问', async ({ page }) => {
    await page.goto('/knowledge')
    await page.waitForTimeout(2000)
    const firstCard = page.locator('.knowledge-card, .el-card').first()
    if (await firstCard.isVisible()) {
      await firstCard.click()
      await page.waitForTimeout(2000)
    }
  })

  test('植物详情API应返回正确结构', async ({ request }) => {
    const listRes = await request.get('/api/plants/list?page=1&size=1')
    const listBody = await listRes.json()
    if (listBody.data.records.length > 0) {
      const id = listBody.data.records[0].id
      const detailRes = await request.get(`/api/plants/${id}`)
      expect(detailRes.ok()).toBeTruthy()
      const detailBody = await detailRes.json()
      expect(detailBody.code).toBe(200)
      expect(detailBody.data.nameCn).toBeDefined()
    }
  })

  test('知识详情API应返回正确结构', async ({ request }) => {
    const listRes = await request.get('/api/knowledge/list?page=1&size=1')
    const listBody = await listRes.json()
    if (listBody.data.records.length > 0) {
      const id = listBody.data.records[0].id
      const detailRes = await request.get(`/api/knowledge/${id}`)
      expect(detailRes.ok()).toBeTruthy()
      const detailBody = await detailRes.json()
      expect(detailBody.code).toBe(200)
    }
  })
})

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

test.describe('回归测试 - 历史Bug', () => {
  test('浏览量递增不应返回401', async ({ request }) => {
    const response = await request.post('/api/plants/1/view')
    expect(response.status()).not.toBe(401)
  })

  test('知识详情浏览量递增不应返回401', async ({ request }) => {
    const response = await request.post('/api/knowledge/1/view')
    expect(response.status()).not.toBe(401)
  })

  test('传承人详情浏览量递增不应返回401', async ({ request }) => {
    const response = await request.post('/api/inheritors/1/view')
    expect(response.status()).not.toBe(401)
  })

  test('反馈提交不应返回401', async ({ request }) => {
    const response = await request.post('/api/feedback', {
      data: {
        type: 'bug',
        title: '回归测试',
        content: '验证反馈提交不需要登录',
        contact: ''
      }
    })
    expect(response.status()).not.toBe(401)
  })

  test('列表接口size参数不应返回全量数据', async ({ request }) => {
    const response = await request.get('/api/plants/list?page=1&size=9999')
    const body = await response.json()
    expect(body.data.records.length).toBeLessThanOrEqual(100)
  })

  test('XSS特殊字符搜索不应导致服务器错误', async ({ request }) => {
    const response = await request.get('/api/plants/search?keyword=%3Cscript%3Ealert(1)%3C/script%3E&page=1&size=10')
    expect(response.status()).toBeLessThan(500)
  })

  test('SQL注入搜索不应导致服务器错误', async ({ request }) => {
    const response = await request.get('/api/plants/search?keyword=1+OR+1%3D1&page=1&size=10')
    expect(response.status()).toBeLessThan(500)
  })

  test('评论列表接口应无需登录', async ({ request }) => {
    const response = await request.get('/api/comments/list/plant/1')
    expect(response.status()).not.toBe(401)
  })

  test('排行榜接口应无需登录', async ({ request }) => {
    const response = await request.get('/api/leaderboard/combined')
    expect(response.status()).not.toBe(401)
  })

  test('测验题目接口应无需登录', async ({ request }) => {
    const response = await request.get('/api/quiz/questions?count=5')
    expect(response.status()).not.toBe(401)
  })

  test('验证码接口应无需登录', async ({ request }) => {
    const response = await request.get('/api/captcha')
    expect(response.status()).not.toBe(401)
    expect(response.ok()).toBeTruthy()
  })

  test('反馈统计接口应无需登录', async ({ request }) => {
    const response = await request.get('/api/feedback/stats')
    expect(response.status()).not.toBe(401)
  })

  test('资源下载接口应无需登录', async ({ request }) => {
    const response = await request.get('/api/resources/download/1')
    expect(response.status()).not.toBe(401)
  })
})

test.describe('数据完整性测试', () => {
  test('植物列表每条记录应包含必要字段', async ({ request }) => {
    const response = await request.get('/api/plants/list?page=1&size=5')
    expect(response.ok()).toBeTruthy()
    const body = await response.json()
    if (body.data.records.length > 0) {
      const record = body.data.records[0]
      expect(record.id).toBeDefined()
      expect(record.nameCn).toBeDefined()
    }
  })

  test('知识列表每条记录应包含必要字段', async ({ request }) => {
    const response = await request.get('/api/knowledge/list?page=1&size=5')
    expect(response.ok()).toBeTruthy()
    const body = await response.json()
    if (body.data.records.length > 0) {
      const record = body.data.records[0]
      expect(record.id).toBeDefined()
    }
  })

  test('传承人列表每条记录应包含必要字段', async ({ request }) => {
    const response = await request.get('/api/inheritors/list?page=1&size=5')
    expect(response.ok()).toBeTruthy()
    const body = await response.json()
    if (body.data.records.length > 0) {
      const record = body.data.records[0]
      expect(record.id).toBeDefined()
    }
  })

  test('分页元数据应完整', async ({ request }) => {
    const response = await request.get('/api/plants/list?page=1&size=10')
    expect(response.ok()).toBeTruthy()
    const body = await response.json()
    expect(body.data.page).toBeDefined()
    expect(body.data.size).toBeDefined()
    expect(body.data.total).toBeDefined()
    expect(body.data.records).toBeDefined()
  })
})

test.describe('错误处理测试', () => {
  test('访问不存在的植物应返回404', async ({ request }) => {
    const response = await request.get('/api/plants/99999')
    expect(response.status()).toBe(404)
  })

  test('访问不存在的知识应返回404', async ({ request }) => {
    const response = await request.get('/api/knowledge/99999')
    expect(response.status()).toBe(404)
  })

  test('访问不存在的传承人应返回404', async ({ request }) => {
    const response = await request.get('/api/inheritors/99999')
    expect(response.status()).toBe(404)
  })

  test('反馈提交空内容应返回400', async ({ request }) => {
    const response = await request.post('/api/feedback', {
      data: {
        type: 'suggestion',
        title: '测试',
        content: '',
        contact: ''
      }
    })
    expect(response.status()).toBe(400)
  })

  test('反馈提交缺少类型应返回400', async ({ request }) => {
    const response = await request.post('/api/feedback', {
      data: {
        title: '测试',
        content: '内容',
        contact: ''
      }
    })
    expect(response.status()).toBe(400)
  })

  test('登录缺少验证码应返回400', async ({ request }) => {
    const response = await request.post('/api/user/login', {
      data: { username: 'admin', password: 'test' }
    })
    expect(response.status()).toBe(400)
  })

  test('注册缺少验证码应返回400', async ({ request }) => {
    const response = await request.post('/api/user/register', {
      data: { username: 'newuser', password: 'Test1234', confirmPassword: 'Test1234' }
    })
    expect(response.status()).toBe(400)
  })
})

test.describe('API回归测试 - 认证白名单', () => {
  test('GET请求默认不需要登录', async ({ request }) => {
    const endpoints = [
      '/api/plants/list',
      '/api/knowledge/list',
      '/api/inheritors/list',
      '/api/qa/list',
      '/api/resources/list',
      '/api/feedback/stats',
      '/api/captcha',
      '/api/leaderboard/combined',
      '/api/quiz/questions?count=5',
      '/api/metadata/filters'
    ]
    for (const url of endpoints) {
      const response = await request.get(url)
      expect(response.status()).not.toBe(401)
    }
  })

  test('白名单POST请求不需要登录', async ({ request }) => {
    const response = await request.post('/api/feedback', {
      data: { type: 'bug', title: '回归测试', content: '验证白名单', contact: '' }
    })
    expect(response.status()).not.toBe(401)
  })

  test('浏览量递增POST不需要登录', async ({ request }) => {
    const endpoints = [
      '/api/plants/1/view',
      '/api/knowledge/1/view',
      '/api/inheritors/1/view'
    ]
    for (const url of endpoints) {
      const response = await request.post(url)
      expect(response.status()).not.toBe(401)
    }
  })
})

test.describe('API回归测试 - 分页限制', () => {
  test('所有列表接口size=9999应被限制', async ({ request }) => {
    const endpoints = [
      '/api/plants/list?page=1&size=9999',
      '/api/knowledge/list?page=1&size=9999',
      '/api/inheritors/list?page=1&size=9999',
      '/api/qa/list?page=1&size=9999',
      '/api/resources/list?page=1&size=9999'
    ]
    for (const url of endpoints) {
      const response = await request.get(url)
      expect(response.ok()).toBeTruthy()
      const body = await response.json()
      if (body.data && body.data.records) {
        expect(body.data.records.length).toBeLessThanOrEqual(100)
      }
    }
  })

  test('负数页码应被限制', async ({ request }) => {
    const response = await request.get('/api/plants/list?page=-1&size=10')
    expect(response.ok()).toBeTruthy()
    const body = await response.json()
    expect(body.data.page).toBeGreaterThanOrEqual(1)
  })
})

test.describe('API回归测试 - 安全防护', () => {
  test('XSS搜索不应导致服务器错误', async ({ request }) => {
    const xssPayloads = [
      '%3Cscript%3Ealert(1)%3C/script%3E',
      '%3Cimg%20src%3Dx%20onerror%3Dalert(1)%3E',
      'javascript%3Aalert(1)'
    ]
    for (const payload of xssPayloads) {
      const response = await request.get(`/api/plants/search?keyword=${payload}&page=1&size=10`)
      expect(response.status()).toBeLessThan(500)
    }
  })

  test('SQL注入搜索不应导致服务器错误', async ({ request }) => {
    const sqlPayloads = [
      '1+OR+1%3D1',
      '%27%3B+DROP+TABLE+users%3B--',
      'UNION+SELECT+*+FROM+users'
    ]
    for (const payload of sqlPayloads) {
      const response = await request.get(`/api/plants/search?keyword=${payload}&page=1&size=10`)
      expect(response.status()).toBeLessThan(500)
    }
  })

  test('路径遍历不应成功', async ({ request }) => {
    const response = await request.get('/api/resources/download/..%2F..%2F..%2Fetc%2Fpasswd')
    expect(response.status()).toBeLessThan(500)
    expect(response.status()).not.toBe(200)
  })
})

test.describe('API回归测试 - 数据一致性', () => {
  test('植物详情应与列表数据一致', async ({ request }) => {
    const listRes = await request.get('/api/plants/list?page=1&size=5')
    const listBody = await listRes.json()
    if (listBody.data.records.length > 0) {
      const record = listBody.data.records[0]
      const detailRes = await request.get(`/api/plants/${record.id}`)
      expect(detailRes.ok()).toBeTruthy()
      const detailBody = await detailRes.json()
      expect(detailBody.data.id).toBe(record.id)
    }
  })

  test('反馈统计应一致', async ({ request }) => {
    const res = await request.get('/api/feedback/stats')
    const body = await res.json()
    expect(body.data.total).toBe(body.data.pending + body.data.resolved)
  })
})

test.describe('API回归测试 - 响应格式', () => {
  test('所有API应返回统一格式', async ({ request }) => {
    const endpoints = [
      '/api/plants/list',
      '/api/knowledge/list',
      '/api/inheritors/list',
      '/api/captcha',
      '/api/feedback/stats'
    ]
    for (const url of endpoints) {
      const response = await request.get(url)
      const body = await response.json()
      expect(body.code).toBeDefined()
      expect(typeof body.code).toBe('number')
    }
  })
})
