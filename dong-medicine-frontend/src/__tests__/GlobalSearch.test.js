import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import ElementPlus from 'element-plus'

vi.mock('@/utils/request', () => ({
  default: {
    get: vi.fn().mockResolvedValue({ code: 200, data: { records: [], total: 0 } }),
    post: vi.fn().mockResolvedValue({ code: 200 })
  }
}))

vi.mock('vue-router', () => ({
  useRoute: () => ({ query: {} }),
  useRouter: () => ({ push: vi.fn() })
}))

import GlobalSearch from '@/views/GlobalSearch.vue'
import request from '@/utils/request'

function mountComponent(options = {}) {
  return mount(GlobalSearch, {
    global: {
      plugins: [createPinia(), ElementPlus],
      stubs: {
        'el-autocomplete': { template: '<input />' },
        'el-button': { template: '<button><slot /></button>' },
        'el-tabs': { template: '<div><slot /></div>' },
        'el-tab-pane': { template: '<div><slot /></div>' },
        'el-tag': { template: '<span><slot /></span>' },
        'el-empty': { template: '<div><slot /></div>' },
        'el-icon': { template: '<span><slot /></span>' },
        'Pagination': { template: '<div />' },
      }
    },
    ...options
  })
}

describe('GlobalSearch页面 - 渲染', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
    localStorage.clear()
  })

  it('应渲染搜索页面', () => {
    const wrapper = mountComponent()
    expect(wrapper.find('.search-page').exists()).toBe(true)
    expect(wrapper.text()).toContain('全局搜索')
  })

  it('应显示页面副标题', () => {
    const wrapper = mountComponent()
    expect(wrapper.text()).toContain('搜索知识、植物、传承人、问答、资源')
  })

  it('应显示热门搜索标签', () => {
    const wrapper = mountComponent()
    expect(wrapper.text()).toContain('热门搜索')
    expect(wrapper.text()).toContain('侗医药')
    expect(wrapper.text()).toContain('药浴')
  })

  it('应渲染搜索输入框', () => {
    const wrapper = mountComponent()
    expect(wrapper.find('.search-autocomplete').exists()).toBe(true)
  })

  it('应渲染搜索按钮', () => {
    const wrapper = mountComponent()
    expect(wrapper.text()).toContain('搜索')
  })
})

describe('GlobalSearch页面 - 搜索历史管理', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
    localStorage.clear()
  })

  it('loadHistory应从localStorage读取历史', () => {
    localStorage.setItem('dong_search_history', JSON.stringify(['药浴', '钩藤']))
    const wrapper = mountComponent()
    expect(wrapper.vm.searchHistory).toEqual(['药浴', '钩藤'])
  })

  it('localStorage无数据时loadHistory应返回空数组', () => {
    const wrapper = mountComponent()
    expect(wrapper.vm.searchHistory).toEqual([])
  })

  it('addToHistory应添加关键词到历史', () => {
    const wrapper = mountComponent()
    wrapper.vm.addToHistory('钩藤')
    expect(wrapper.vm.searchHistory).toContain('钩藤')
  })

  it('addToHistory应去重并置顶', () => {
    const wrapper = mountComponent()
    wrapper.vm.addToHistory('药浴')
    wrapper.vm.addToHistory('钩藤')
    wrapper.vm.addToHistory('药浴')
    expect(wrapper.vm.searchHistory[0]).toBe('药浴')
    expect(wrapper.vm.searchHistory.filter(h => h === '药浴')).toHaveLength(1)
  })

  it('addToHistory应限制最大历史数量为10', () => {
    const wrapper = mountComponent()
    for (let i = 0; i < 15; i++) {
      wrapper.vm.addToHistory(`关键词${i}`)
    }
    expect(wrapper.vm.searchHistory.length).toBeLessThanOrEqual(10)
  })

  it('addToHistory应忽略空字符串', () => {
    const wrapper = mountComponent()
    wrapper.vm.addToHistory('')
    wrapper.vm.addToHistory('   ')
    expect(wrapper.vm.searchHistory).toEqual([])
  })

  it('removeHistory应移除指定索引的历史', () => {
    const wrapper = mountComponent()
    wrapper.vm.addToHistory('药浴')
    wrapper.vm.addToHistory('钩藤')
    // addToHistory prepends, so history is ['钩藤', '药浴']
    wrapper.vm.removeHistory(0)
    expect(wrapper.vm.searchHistory).not.toContain('钩藤')
    expect(wrapper.vm.searchHistory).toContain('药浴')
  })

  it('clearHistory应清空所有历史', () => {
    const wrapper = mountComponent()
    wrapper.vm.addToHistory('药浴')
    wrapper.vm.addToHistory('钩藤')
    wrapper.vm.clearHistory()
    expect(wrapper.vm.searchHistory).toEqual([])
  })

  it('saveHistory应写入localStorage', () => {
    const wrapper = mountComponent()
    wrapper.vm.addToHistory('测试关键词')
    const stored = JSON.parse(localStorage.getItem('dong_search_history'))
    expect(stored).toContain('测试关键词')
  })
})

describe('GlobalSearch页面 - 工具函数', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
    localStorage.clear()
  })

  it('escapeRegex应转义正则特殊字符', () => {
    const wrapper = mountComponent()
    expect(wrapper.vm.escapeRegex('test.com')).toBe('test\\.com')
    expect(wrapper.vm.escapeRegex('a+b*c')).toBe('a\\+b\\*c')
    expect(wrapper.vm.escapeRegex('[test]')).toBe('\\[test\\]')
    expect(wrapper.vm.escapeRegex('(foo|bar)')).toBe('\\(foo\\|bar\\)')
    expect(wrapper.vm.escapeRegex('a?b')).toBe('a\\?b')
    expect(wrapper.vm.escapeRegex('$100')).toBe('\\$100')
  })

  it('escapeRegex不应转义普通字符', () => {
    const wrapper = mountComponent()
    expect(wrapper.vm.escapeRegex('钩藤')).toBe('钩藤')
    expect(wrapper.vm.escapeRegex('hello')).toBe('hello')
  })

  it('escapeHtml应转义HTML实体', () => {
    const wrapper = mountComponent()
    expect(wrapper.vm.escapeHtml('<script>alert(1)</script>')).toBe('&lt;script&gt;alert(1)&lt;/script&gt;')
    expect(wrapper.vm.escapeHtml('a & b')).toBe('a &amp; b')
    expect(wrapper.vm.escapeHtml('"hello"')).toBe('&quot;hello&quot;')
  })

  it('escapeHtml应处理空值', () => {
    const wrapper = mountComponent()
    expect(wrapper.vm.escapeHtml('')).toBe('')
    expect(wrapper.vm.escapeHtml(null)).toBe('')
    expect(wrapper.vm.escapeHtml(undefined)).toBe('')
  })

  it('highlightText应高亮关键词', () => {
    const wrapper = mountComponent()
    wrapper.vm.keyword = '钩藤'
    wrapper.vm.lastKeyword = '钩藤'
    const result = wrapper.vm.highlightText('钩藤是一种药材')
    expect(result).toContain('<em class="highlight">钩藤</em>')
  })

  it('highlightText应忽略大小写', () => {
    const wrapper = mountComponent()
    wrapper.vm.lastKeyword = 'abc'
    const result = wrapper.vm.highlightText('ABC abc AbC')
    expect(result).toContain('<em class="highlight">ABC</em>')
    expect(result).toContain('<em class="highlight">abc</em>')
    expect(result).toContain('<em class="highlight">AbC</em>')
  })

  it('highlightText应转义HTML防止XSS', () => {
    const wrapper = mountComponent()
    wrapper.vm.lastKeyword = '<script>'
    const result = wrapper.vm.highlightText('<script>')
    expect(result).not.toContain('<script>')
    expect(result).toContain('&lt;')
  })

  it('highlightText无关键词时应返回转义后的文本', () => {
    const wrapper = mountComponent()
    wrapper.vm.keyword = ''
    wrapper.vm.lastKeyword = ''
    const result = wrapper.vm.highlightText('普通文本')
    expect(result).toBe('普通文本')
    expect(result).not.toContain('<em')
  })

  it('highlightText空文本应返回空字符串', () => {
    const wrapper = mountComponent()
    expect(wrapper.vm.highlightText('')).toBe('')
    expect(wrapper.vm.highlightText(null)).toBe('')
  })

  it('getData应处理数组格式的响应数据', () => {
    const wrapper = mountComponent()
    const result = wrapper.vm.getData({ data: [{ id: 1 }, { id: 2 }] })
    expect(result.records).toHaveLength(2)
    expect(result.total).toBe(2)
  })

  it('getData应处理records格式的响应数据', () => {
    const wrapper = mountComponent()
    const result = wrapper.vm.getData({ data: { records: [{ id: 1 }], total: 5 } })
    expect(result.records).toHaveLength(1)
    expect(result.total).toBe(5)
  })

  it('getData应处理空数据', () => {
    const wrapper = mountComponent()
    const result = wrapper.vm.getData({ data: null })
    expect(result.records).toEqual([])
    expect(result.total).toBe(0)
  })

  it('getData应处理无data字段的响应', () => {
    const wrapper = mountComponent()
    const result = wrapper.vm.getData({})
    expect(result.records).toEqual([])
    expect(result.total).toBe(0)
  })
})

describe('GlobalSearch页面 - 类型映射函数', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  it('getTypeTag应返回正确的标签类型', () => {
    const wrapper = mountComponent()
    expect(wrapper.vm.getTypeTag('knowledge')).toBe('primary')
    expect(wrapper.vm.getTypeTag('plant')).toBe('success')
    expect(wrapper.vm.getTypeTag('inheritor')).toBe('warning')
    expect(wrapper.vm.getTypeTag('qa')).toBe('info')
    expect(wrapper.vm.getTypeTag('resource')).toBe('danger')
  })

  it('getTypeTag未知类型应返回info', () => {
    const wrapper = mountComponent()
    expect(wrapper.vm.getTypeTag('unknown')).toBe('info')
  })

  it('getTypeName应返回正确的显示名称', () => {
    const wrapper = mountComponent()
    expect(wrapper.vm.getTypeName('knowledge')).toBe('知识')
    expect(wrapper.vm.getTypeName('plant')).toBe('植物')
    expect(wrapper.vm.getTypeName('inheritor')).toBe('传承人')
    expect(wrapper.vm.getTypeName('qa')).toBe('问答')
    expect(wrapper.vm.getTypeName('resource')).toBe('资源')
  })

  it('getTypeName未知类型应返回其他', () => {
    const wrapper = mountComponent()
    expect(wrapper.vm.getTypeName('unknown')).toBe('其他')
  })

  it('getTypeIcon应返回正确的图标组件', () => {
    const wrapper = mountComponent()
    const iconTypes = ['knowledge', 'plant', 'inheritor', 'qa', 'resource']
    iconTypes.forEach(type => {
      expect(wrapper.vm.getTypeIcon(type)).toBeDefined()
    })
  })
})

describe('GlobalSearch页面 - 搜索操作', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
    localStorage.clear()
  })

  it('doSearch应调用5个API端点', async () => {
    request.get.mockResolvedValue({ code: 200, data: { records: [], total: 0 } })
    const wrapper = mountComponent()
    wrapper.vm.keyword = '钩藤'
    await wrapper.vm.doSearch()
    expect(request.get).toHaveBeenCalledTimes(5)
    expect(request.get).toHaveBeenCalledWith(expect.stringContaining('/knowledge/search'))
    expect(request.get).toHaveBeenCalledWith(expect.stringContaining('/plants/search'))
    expect(request.get).toHaveBeenCalledWith(expect.stringContaining('/inheritors/search'))
    expect(request.get).toHaveBeenCalledWith(expect.stringContaining('/qa/search'))
    expect(request.get).toHaveBeenCalledWith(expect.stringContaining('/resources/search'))
  })

  it('doSearch应设置searched为true', async () => {
    request.get.mockResolvedValue({ code: 200, data: { records: [], total: 0 } })
    const wrapper = mountComponent()
    wrapper.vm.keyword = '钩藤'
    await wrapper.vm.doSearch()
    expect(wrapper.vm.searched).toBe(true)
  })

  it('doSearch应将关键词添加到历史', async () => {
    request.get.mockResolvedValue({ code: 200, data: { records: [], total: 0 } })
    const wrapper = mountComponent()
    wrapper.vm.keyword = '钩藤'
    await wrapper.vm.doSearch()
    expect(wrapper.vm.searchHistory).toContain('钩藤')
  })

  it('doSearch空关键词应清空搜索状态', async () => {
    const wrapper = mountComponent()
    wrapper.vm.keyword = ''
    await wrapper.vm.doSearch()
    expect(wrapper.vm.searched).toBe(false)
    expect(request.get).not.toHaveBeenCalled()
  })

  it('doSearch应合并各类型结果', async () => {
    request.get
      .mockResolvedValueOnce({ code: 200, data: { records: [{ id: 1 }], total: 1 } })
      .mockResolvedValueOnce({ code: 200, data: { records: [{ id: 2 }], total: 1 } })
      .mockResolvedValueOnce({ code: 200, data: { records: [], total: 0 } })
      .mockResolvedValueOnce({ code: 200, data: { records: [], total: 0 } })
      .mockResolvedValueOnce({ code: 200, data: { records: [], total: 0 } })
    const wrapper = mountComponent()
    wrapper.vm.keyword = '测试'
    await wrapper.vm.doSearch()
    expect(wrapper.vm.allResults.length).toBe(2)
    expect(wrapper.vm.allResults[0].type).toBe('knowledge')
    expect(wrapper.vm.allResults[1].type).toBe('plant')
  })

  it('doSearch应设置typeCounts', async () => {
    request.get
      .mockResolvedValueOnce({ code: 200, data: { records: [{ id: 1 }, { id: 2 }], total: 2 } })
      .mockResolvedValueOnce({ code: 200, data: { records: [{ id: 3 }], total: 1 } })
      .mockResolvedValueOnce({ code: 200, data: { records: [], total: 0 } })
      .mockResolvedValueOnce({ code: 200, data: { records: [], total: 0 } })
      .mockResolvedValueOnce({ code: 200, data: { records: [], total: 0 } })
    const wrapper = mountComponent()
    wrapper.vm.keyword = '测试'
    await wrapper.vm.doSearch()
    expect(wrapper.vm.typeCounts.knowledge).toBe(2)
    expect(wrapper.vm.typeCounts.plant).toBe(1)
  })

  it('doSearch API失败时应优雅降级', async () => {
    request.get.mockRejectedValue(new Error('网络错误'))
    const wrapper = mountComponent()
    wrapper.vm.keyword = '测试'
    await wrapper.vm.doSearch()
    expect(wrapper.vm.searched).toBe(true)
    expect(wrapper.vm.allResults).toEqual([])
  })
})

describe('GlobalSearch页面 - 导航和交互', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  it('goToDetail应根据类型导航到正确路由', () => {
    const wrapper = mountComponent()
    const pushSpy = vi.spyOn(wrapper.vm.router, 'push')
    wrapper.vm.goToDetail({ type: 'knowledge' })
    expect(pushSpy).toHaveBeenCalledWith('/knowledge')
  })

  it('goToDetail植物类型应导航到/plants', () => {
    const wrapper = mountComponent()
    const pushSpy = vi.spyOn(wrapper.vm.router, 'push')
    wrapper.vm.goToDetail({ type: 'plant' })
    expect(pushSpy).toHaveBeenCalledWith('/plants')
  })

  it('goToDetail传承人类型应导航到/inheritors', () => {
    const wrapper = mountComponent()
    const pushSpy = vi.spyOn(wrapper.vm.router, 'push')
    wrapper.vm.goToDetail({ type: 'inheritor' })
    expect(pushSpy).toHaveBeenCalledWith('/inheritors')
  })

  it('goToDetail问答类型应导航到/qa', () => {
    const wrapper = mountComponent()
    const pushSpy = vi.spyOn(wrapper.vm.router, 'push')
    wrapper.vm.goToDetail({ type: 'qa' })
    expect(pushSpy).toHaveBeenCalledWith('/qa')
  })

  it('goToDetail资源类型应导航到/resources', () => {
    const wrapper = mountComponent()
    const pushSpy = vi.spyOn(wrapper.vm.router, 'push')
    wrapper.vm.goToDetail({ type: 'resource' })
    expect(pushSpy).toHaveBeenCalledWith('/resources')
  })

  it('onClear应重置所有搜索状态', () => {
    const wrapper = mountComponent()
    wrapper.vm.keyword = '测试'
    wrapper.vm.lastKeyword = '测试'
    wrapper.vm.searched = true
    wrapper.vm.allResults = [{ id: 1 }]
    wrapper.vm.total = 1
    wrapper.vm.onClear()
    expect(wrapper.vm.keyword).toBe('')
    expect(wrapper.vm.lastKeyword).toBe('')
    expect(wrapper.vm.searched).toBe(false)
    expect(wrapper.vm.allResults).toEqual([])
    expect(wrapper.vm.total).toBe(0)
    expect(wrapper.vm.activeTab).toBe('all')
    expect(wrapper.vm.currentPage).toBe(1)
  })
})

describe('GlobalSearch页面 - 分页计算属性', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  it('paginatedResults应根据当前页和页大小切分结果', () => {
    const wrapper = mountComponent()
    const items = Array.from({ length: 25 }, (_, i) => ({ id: i, type: 'knowledge' }))
    wrapper.vm.allResults = items
    wrapper.vm.currentPage = 1
    wrapper.vm.pageSize = 12
    expect(wrapper.vm.paginatedResults.length).toBe(12)
  })

  it('paginatedResults第二页应返回正确数据', () => {
    const wrapper = mountComponent()
    const items = Array.from({ length: 25 }, (_, i) => ({ id: i, type: 'knowledge' }))
    wrapper.vm.allResults = items
    wrapper.vm.currentPage = 2
    wrapper.vm.pageSize = 12
    expect(wrapper.vm.paginatedResults.length).toBe(12)
    expect(wrapper.vm.paginatedResults[0].id).toBe(12)
  })

  it('paginatedResults最后一页应返回剩余数据', () => {
    const wrapper = mountComponent()
    const items = Array.from({ length: 25 }, (_, i) => ({ id: i, type: 'knowledge' }))
    wrapper.vm.allResults = items
    wrapper.vm.currentPage = 3
    wrapper.vm.pageSize = 12
    expect(wrapper.vm.paginatedResults.length).toBe(1)
  })

  it('activeTab非all时应返回searchResults', () => {
    const wrapper = mountComponent()
    wrapper.vm.activeTab = 'plant'
    wrapper.vm.searchResults = [{ id: 1, type: 'plant' }]
    wrapper.vm.allResults = [{ id: 2, type: 'knowledge' }]
    expect(wrapper.vm.paginatedResults).toEqual([{ id: 1, type: 'plant' }])
  })
})

describe('GlobalSearch页面 - 回归测试', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
    localStorage.clear()
  })

  it('Bug: localStorage返回无效JSON时不应崩溃', () => {
    localStorage.setItem('dong_search_history', 'invalid json {{{')
    expect(() => mountComponent()).not.toThrow()
    const wrapper = mountComponent()
    expect(wrapper.vm.searchHistory).toEqual([])
  })

  it('Bug: highlightText含正则特殊字符的关键词不应崩溃', () => {
    const wrapper = mountComponent()
    wrapper.vm.lastKeyword = 'a+b.c*d'
    expect(() => wrapper.vm.highlightText('a+b.c*d是一种药材')).not.toThrow()
  })

  it('Bug: doSearch并发调用不应产生重复历史记录', async () => {
    request.get.mockResolvedValue({ code: 200, data: { records: [], total: 0 } })
    const wrapper = mountComponent()
    wrapper.vm.keyword = '并发测试'
    await Promise.all([wrapper.vm.doSearch(), wrapper.vm.doSearch()])
    expect(wrapper.vm.searchHistory.filter(h => h === '并发测试').length).toBeLessThanOrEqual(1)
  })

  it('Bug: escapeHtml含所有特殊字符时不应崩溃', () => {
    const wrapper = mountComponent()
    expect(() => wrapper.vm.escapeHtml('<>&"\'test')).not.toThrow()
    const result = wrapper.vm.escapeHtml('<>&"\'test')
    expect(result).toBe('&lt;&gt;&amp;&quot;&#x27;test')
  })

  it('Bug: getData返回records为非数组时应降级为空', () => {
    const wrapper = mountComponent()
    const result = wrapper.vm.getData({ data: { records: 'not-array', total: 5 } })
    expect(result.records).toEqual([])
    expect(result.total).toBe(0)
  })
})
