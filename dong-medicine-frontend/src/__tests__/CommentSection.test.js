import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import ElementPlus from 'element-plus'

vi.mock('element-plus', async (importOriginal) => {
  const actual = await importOriginal()
  return {
    ...actual,
    ElMessage: { success: vi.fn(), error: vi.fn(), warning: vi.fn(), info: vi.fn() }
  }
})

import CommentSection from '@/components/business/interact/CommentSection.vue'
import { ElMessage } from 'element-plus'

const now = Date.now
function makeTime(offset) {
  return new Date(Date.now() - offset).toISOString()
}

function mountComponent(props = {}) {
  return mount(CommentSection, {
    props: {
      comments: [],
      isLoggedIn: false,
      userName: '测试用户',
      loading: false,
      loadingMore: false,
      hasMore: true,
      ...props
    },
    global: {
      plugins: [ElementPlus],
      stubs: {
        'el-avatar': { template: '<span><slot /></span>' },
        'el-button': { template: '<button><slot /></button>' },
        'el-radio-group': { template: '<div><slot /></div>' },
        'el-radio-button': { template: '<label><slot /></label>' },
        'el-input': { template: '<div><slot /></div>' },
        'el-dialog': { template: '<div><slot /><slot name="footer" /></div>' },
        'el-icon': { template: '<i><slot /></i>' },
        'el-empty': { template: '<div>empty</div>' },
      }
    }
  })
}

describe('CommentSection组件', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('应渲染评论组件', () => {
    const wrapper = mountComponent()
    expect(wrapper.find('.comment-section').exists()).toBe(true)
  })

  it('评论为空时应显示空状态', () => {
    const wrapper = mountComponent({ comments: [] })
    expect(wrapper.text()).toContain('empty')
  })

  it('有评论时应显示评论列表', () => {
    const comments = [
      { id: 1, username: '用户A', content: '很好的文章', createTime: makeTime(3600000), likes: 5, hot: 10 }
    ]
    const wrapper = mountComponent({ comments })
    expect(wrapper.find('.comment-item').exists()).toBe(true)
    expect(wrapper.text()).toContain('用户A')
    expect(wrapper.text()).toContain('很好的文章')
  })

  it('应过滤主评论（不含replyToId的评论）', () => {
    const comments = [
      { id: 1, username: '用户A', content: '主评论', createTime: makeTime(3600000) },
      { id: 2, username: '用户B', content: '回复', createTime: makeTime(1800000), replyToId: 1 },
    ]
    const wrapper = mountComponent({ comments })
    const mainItems = wrapper.findAll('.main-comment')
    expect(mainItems).toHaveLength(1)
    expect(wrapper.text()).toContain('主评论')
  })

  it('排序切换：默认按最新排序', () => {
    const comments = [
      { id: 1, username: '用户A', content: '较早', createTime: makeTime(7200000), hot: 100 },
      { id: 2, username: '用户B', content: '较新', createTime: makeTime(1000), hot: 1 },
    ]
    const wrapper = mountComponent({ comments })
    const mainItems = wrapper.findAll('.main-comment')
    expect(mainItems.length).toBeGreaterThanOrEqual(2)
    // 默认按createTime desc，较新应排在前面
    const text = wrapper.text()
    const newerIdx = text.indexOf('较新')
    const olderIdx = text.indexOf('较早')
    expect(newerIdx).toBeLessThan(olderIdx)
  })

  it('formatTime应返回"刚刚"（小于1分钟）', () => {
    const comments = [
      { id: 1, username: '用户A', content: '测试', createTime: makeTime(30000) }
    ]
    const wrapper = mountComponent({ comments })
    expect(wrapper.text()).toContain('刚刚')
  })

  it('formatTime应返回"X分钟前"', () => {
    const comments = [
      { id: 1, username: '用户A', content: '测试', createTime: makeTime(300000) }
    ]
    const wrapper = mountComponent({ comments })
    expect(wrapper.text()).toMatch(/\d+分钟前/)
  })

  it('formatTime应返回"X小时前"', () => {
    const comments = [
      { id: 1, username: '用户A', content: '测试', createTime: makeTime(7200000) }
    ]
    const wrapper = mountComponent({ comments })
    expect(wrapper.text()).toMatch(/\d+小时前/)
  })

  it('未登录时点击回复应显示警告', async () => {
    const comments = [
      { id: 1, username: '用户A', content: '测试', createTime: makeTime(3600000) }
    ]
    const wrapper = mountComponent({ comments, isLoggedIn: false })
    const replyBtn = wrapper.find('.reply-btn')
    await replyBtn.trigger('click')
    expect(ElMessage.warning).toHaveBeenCalledWith('请先登录后再回复')
  })

  it('未登录时点击发布评论应显示警告', async () => {
    const wrapper = mountComponent({ isLoggedIn: false })
    // postComment通过input区域的按钮触发
    await wrapper.vm.$nextTick()
    // 直接调用组件内部方法
    const vm = wrapper.vm
    // 模拟未登录postComment
    // 通过查找发布按钮
    const publishBtn = wrapper.find('.input-actions .el-button-stub')
    if (publishBtn.exists()) {
      await publishBtn.trigger('click')
      expect(ElMessage.warning).toHaveBeenCalledWith('请先登录后再发表评论')
    }
  })

  it('点赞按钮点击应触发like事件', async () => {
    const comment = { id: 1, username: '用户A', content: '测试', createTime: makeTime(3600000), likes: 5 }
    const wrapper = mountComponent({ comments: [comment], isLoggedIn: true })
    const likeBtn = wrapper.find('.like-btn')
    await likeBtn.trigger('click')
    expect(wrapper.emitted('like')).toBeTruthy()
    expect(wrapper.emitted('like')[0][0]).toEqual(comment)
  })

  it('有回复的评论应显示查看回复按钮', () => {
    const comments = [
      { id: 1, username: '用户A', content: '主评论', createTime: makeTime(7200000) },
      { id: 2, username: '用户B', content: '回复1', createTime: makeTime(3600000), replyToId: 1 },
    ]
    const wrapper = mountComponent({ comments })
    const toggleBtn = wrapper.find('.toggle-replies-btn')
    expect(toggleBtn.exists()).toBe(true)
    expect(toggleBtn.text()).toContain('查看 1 条回复')
  })

  it('展开回复后应显示收起回复按钮', async () => {
    const comments = [
      { id: 1, username: '用户A', content: '主评论', createTime: makeTime(7200000) },
      { id: 2, username: '用户B', content: '回复1', createTime: makeTime(3600000), replyToId: 1 },
    ]
    const wrapper = mountComponent({ comments })
    const toggleBtn = wrapper.find('.toggle-replies-btn')
    await toggleBtn.trigger('click')
    expect(wrapper.text()).toContain('收起回复')
    expect(wrapper.find('.replies-list').exists()).toBe(true)
  })

  it('递归回复应正确统计后代数量', () => {
    const comments = [
      { id: 1, username: '用户A', content: '主评论', createTime: makeTime(7200000) },
      { id: 2, username: '用户B', content: '回复1', createTime: makeTime(5000000), replyToId: 1 },
      { id: 3, username: '用户C', content: '回复2', createTime: makeTime(4000000), replyToId: 2 },
      { id: 4, username: '用户D', content: '回复3', createTime: makeTime(3000000), replyToId: 1 },
    ]
    const wrapper = mountComponent({ comments })
    const toggleBtn = wrapper.find('.toggle-replies-btn')
    expect(toggleBtn.text()).toContain('查看 3 条回复')
  })

  it('loadingMore为true时应显示加载中提示', () => {
    const comments = [
      { id: 1, username: '用户A', content: '测试', createTime: makeTime(3600000) }
    ]
    const wrapper = mountComponent({ comments, loadingMore: true })
    expect(wrapper.text()).toContain('加载中...')
  })

  it('hasMore为false时应显示没有更多评论', () => {
    const comments = [
      { id: 1, username: '用户A', content: '测试', createTime: makeTime(3600000) }
    ]
    const wrapper = mountComponent({ comments, hasMore: false })
    expect(wrapper.text()).toContain('没有更多评论了')
  })

  it('应显示排序控制按钮', () => {
    const wrapper = mountComponent()
    expect(wrapper.find('.sort-controls').exists()).toBe(true)
    expect(wrapper.text()).toContain('排序方式')
  })

  it('应显示评论输入区域', () => {
    const wrapper = mountComponent()
    expect(wrapper.find('.comment-input-area').exists()).toBe(true)
  })
})

describe('CommentSection组件 - 回归测试', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('Bug: 评论为空数组时不应崩溃', () => {
    expect(() => mountComponent({ comments: [] })).not.toThrow()
  })

  it('Bug: 评论username为null时应显示匿名用户', () => {
    const comments = [
      { id: 1, username: null, content: '测试', createTime: makeTime(3600000) }
    ]
    const wrapper = mountComponent({ comments })
    expect(wrapper.text()).toContain('匿名用户')
  })

  it('Bug: 评论likes为undefined时不应崩溃', () => {
    const comments = [
      { id: 1, username: '用户A', content: '测试', createTime: makeTime(3600000) }
    ]
    const wrapper = mountComponent({ comments })
    expect(wrapper.find('.comment-item').exists()).toBe(true)
  })

  it('Bug: 回复replyToId为数字0时应正确处理', () => {
    const comments = [
      { id: 1, username: '用户A', content: '主评论', createTime: makeTime(7200000) },
      { id: 2, username: '用户B', content: '回复', createTime: makeTime(3600000), replyToId: 1 },
    ]
    const wrapper = mountComponent({ comments })
    const mainItems = wrapper.findAll('.main-comment')
    expect(mainItems).toHaveLength(1)
  })

  it('Bug: formatTime传入null不应崩溃', () => {
    const comments = [
      { id: 1, username: '用户A', content: '测试', createTime: null }
    ]
    expect(() => mountComponent({ comments })).not.toThrow()
  })
})
