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
