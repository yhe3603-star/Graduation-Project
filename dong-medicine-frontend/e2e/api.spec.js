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
