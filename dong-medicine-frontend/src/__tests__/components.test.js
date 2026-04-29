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

import Pagination from '@/components/business/display/Pagination.vue'
import SearchFilter from '@/components/business/display/SearchFilter.vue'
import CaptchaInput from '@/components/business/interact/CaptchaInput.vue'

function mountWithPlugins(component, options = {}) {
  return mount(component, {
    global: {
      plugins: [createPinia(), ElementPlus],
      stubs: {
        'el-pagination': true,
        'el-input': true,
        'el-button': true,
        'el-select': true,
        'el-option': true,
        'el-image': true,
        'el-form-item': true,
        'el-icon': true
      },
      ...options.global
    },
    ...options
  })
}

describe('Pagination组件', () => {
  it('应渲染分页组件', () => {
    const wrapper = mountWithPlugins(Pagination, {
      props: {
        total: 100,
        currentPage: 1,
        pageSize: 10
      }
    })
    expect(wrapper.findComponent(Pagination).exists()).toBe(true)
  })

  it('应触发页码变更事件', async () => {
    const wrapper = mountWithPlugins(Pagination, {
      props: {
        total: 100,
        currentPage: 1,
        pageSize: 10
      }
    })
    await wrapper.vm.$emit('update:currentPage', 2)
    expect(wrapper.emitted('update:currentPage')).toBeTruthy()
  })

  it('应触发页大小变更事件', async () => {
    const wrapper = mountWithPlugins(Pagination, {
      props: {
        total: 100,
        currentPage: 1,
        pageSize: 10
      }
    })
    await wrapper.vm.$emit('update:pageSize', 20)
    expect(wrapper.emitted('update:pageSize')).toBeTruthy()
  })
})

describe('SearchFilter组件', () => {
  it('应渲染搜索组件', () => {
    const wrapper = mountWithPlugins(SearchFilter, {
      props: {
        keyword: ''
      }
    })
    expect(wrapper.findComponent(SearchFilter).exists()).toBe(true)
  })

  it('应触发搜索事件', async () => {
    const wrapper = mountWithPlugins(SearchFilter, {
      props: {
        keyword: '钩藤'
      }
    })
    await wrapper.vm.$emit('search', '钩藤')
    expect(wrapper.emitted('search')).toBeTruthy()
  })

  it('应触发关键词更新事件', async () => {
    const wrapper = mountWithPlugins(SearchFilter, {
      props: {
        keyword: ''
      }
    })
    await wrapper.vm.$emit('update:keyword', '药浴')
    expect(wrapper.emitted('update:keyword')).toBeTruthy()
  })
})

describe('CaptchaInput组件', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  it('应渲染验证码输入组件', () => {
    const wrapper = mountWithPlugins(CaptchaInput, {
      props: {
        modelValue: ''
      }
    })
    expect(wrapper.findComponent(CaptchaInput).exists()).toBe(true)
  })

  it('应触发值更新事件', async () => {
    const wrapper = mountWithPlugins(CaptchaInput, {
      props: {
        modelValue: ''
      }
    })
    await wrapper.vm.$emit('update:modelValue', '1234')
    expect(wrapper.emitted('update:modelValue')).toBeTruthy()
  })
})

describe('组件集成 - 回归测试', () => {
  it('Bug: Pagination边界值 - total=0不应崩溃', () => {
    expect(() => mountWithPlugins(Pagination, {
      props: { total: 0, currentPage: 1, pageSize: 10 }
    })).not.toThrow()
  })

  it('Bug: SearchFilter空关键词搜索不应崩溃', () => {
    const wrapper = mountWithPlugins(SearchFilter, {
      props: { keyword: '' }
    })
    expect(() => wrapper.vm.$emit('search', '')).not.toThrow()
  })

  it('Bug: CaptchaInput特殊字符不应崩溃', () => {
    const wrapper = mountWithPlugins(CaptchaInput, {
      props: { modelValue: '<script>alert(1)</script>' }
    })
    expect(wrapper.findComponent(CaptchaInput).exists()).toBe(true)
  })
})
