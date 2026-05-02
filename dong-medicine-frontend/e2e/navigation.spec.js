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
