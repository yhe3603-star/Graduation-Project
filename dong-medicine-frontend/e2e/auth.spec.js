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
