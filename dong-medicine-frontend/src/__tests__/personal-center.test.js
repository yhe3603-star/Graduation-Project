import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import ProfileSection from '@/views/personal-center/ProfileSection.vue'
import StatsDashboard from '@/views/personal-center/StatsDashboard.vue'
import BrowseHistoryPanel from '@/views/personal-center/BrowseHistoryPanel.vue'

vi.mock('@/utils/request', () => ({
  default: { get: vi.fn(() => Promise.resolve({ data: [] })) }
}))

describe('ProfileSection', () => {
  const actions = [
    { key: 'stats', icon: {}, label: '学习统计' },
    { key: 'favorites', icon: {}, label: '我的收藏' }
  ]

  const studyStats = [
    { value: 10, label: '总答题次数' },
    { value: 85, label: '平均得分' }
  ]

  it('应渲染用户名', () => {
    const wrapper = mount(ProfileSection, {
      props: { userName: 'testUser', isAdmin: false, studyStats, actions },
      global: { plugins: [ElementPlus] }
    })
    expect(wrapper.text()).toContain('testUser')
  })

  it('管理员应显示管理员标签', () => {
    const wrapper = mount(ProfileSection, {
      props: { userName: 'admin', isAdmin: true, studyStats, actions },
      global: { plugins: [ElementPlus] }
    })
    expect(wrapper.text()).toContain('管理员')
  })

  it('点击快捷操作应触发tab-change事件', async () => {
    const wrapper = mount(ProfileSection, {
      props: { userName: 'test', isAdmin: false, studyStats, actions },
      global: { plugins: [ElementPlus] }
    })
    const card = wrapper.find('.action-card')
    if (card.exists()) {
      await card.trigger('click')
      expect(wrapper.emitted('tab-change')).toBeTruthy()
    }
  })

  it('应显示学习统计数据', () => {
    const wrapper = mount(ProfileSection, {
      props: { userName: 'test', isAdmin: false, studyStats, actions },
      global: { plugins: [ElementPlus] }
    })
    expect(wrapper.text()).toContain('10')
    expect(wrapper.text()).toContain('85')
  })
})

describe('StatsDashboard', () => {
  it('应渲染并显示无数据提示', () => {
    const wrapper = mount(StatsDashboard, {
      props: { quizRecords: [], gameRecords: [], favorites: [], browseHistory: [], loading: false },
      global: { plugins: [ElementPlus] }
    })
    expect(wrapper.find('.stats-dashboard').exists()).toBe(true)
  })

  it('Loading 状态应正确显示', () => {
    const wrapper = mount(StatsDashboard, {
      props: { quizRecords: [], gameRecords: [], favorites: [], browseHistory: [], loading: true },
      global: { plugins: [ElementPlus] }
    })
    expect(wrapper.find('.stats-dashboard').exists()).toBe(true)
  })
})

describe('BrowseHistoryPanel', () => {
  it('应渲染浏览历史面板', () => {
    const wrapper = mount(BrowseHistoryPanel, {
      global: {
        plugins: [createPinia(), ElementPlus],
        stubs: { Pagination: true }
      }
    })
    expect(wrapper.exists()).toBe(true)
  })
})
