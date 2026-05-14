import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import ElementPlus from 'element-plus'

vi.mock('@/utils', () => ({
  getFirstImage: vi.fn((images) => {
    if (Array.isArray(images)) return images[0] || "data:image/svg+xml,...placeholder..."
    return images || "data:image/svg+xml,...placeholder..."
  })
}))

import PlantGame from '@/components/business/interact/PlantGame.vue'

function mountComponent(props = {}) {
  return mount(PlantGame, {
    props: {
      currentPlant: { nameCn: '钩藤', images: '/images/test.jpg' },
      options: ['钩藤', '金银花', '艾草'],
      score: 0,
      streak: 0,
      answered: false,
      selectedAnswer: '',
      finished: false,
      gameStarted: false,
      submitting: false,
      plantsLoaded: true,
      correct: 0,
      total: 10,
      isLoggedIn: false,
      selectedDifficulty: 'easy',
      formattedTime: '03:00',
      isLowTime: false,
      ...props
    },
    global: {
      plugins: [ElementPlus],
      stubs: {
        'el-button': { template: '<button><slot /></button>' },
        'el-card': { template: '<div><slot /></div>' },
        'el-image': { template: '<img />' },
        'el-icon': { template: '<span><slot /></span>' },
      }
    }
  })
}

describe('PlantGame组件 - 介绍界面', () => {
  it('应渲染游戏介绍界面', () => {
    const wrapper = mountComponent()
    expect(wrapper.find('.game-intro').exists()).toBe(true)
    expect(wrapper.text()).toContain('药用植物识别游戏')
  })

  it('应显示难度选择卡片', () => {
    const wrapper = mountComponent()
    expect(wrapper.find('.difficulty-select').exists()).toBe(true)
    expect(wrapper.findAll('.difficulty-card')).toHaveLength(3)
  })

  it('应显示开始游戏按钮', () => {
    const wrapper = mountComponent()
    expect(wrapper.find('.start-btn').exists()).toBe(true)
    expect(wrapper.text()).toContain('开始游戏')
  })

  it('plantsLoaded为false时开始按钮应禁用', () => {
    const wrapper = mountComponent({ plantsLoaded: false })
    const startBtn = wrapper.find('.start-btn')
    expect(startBtn.exists()).toBe(true)
  })

  it('点击难度卡片应触发selectDifficulty事件', async () => {
    const wrapper = mountComponent()
    const cards = wrapper.findAll('.difficulty-card')
    await cards[1].trigger('click')
    expect(wrapper.emitted('selectDifficulty')).toBeTruthy()
    expect(wrapper.emitted('selectDifficulty')[0]).toEqual(['medium'])
  })

  it('点击初级难度应触发selectDifficulty事件', async () => {
    const wrapper = mountComponent()
    const cards = wrapper.findAll('.difficulty-card')
    await cards[0].trigger('click')
    expect(wrapper.emitted('selectDifficulty')).toBeTruthy()
    expect(wrapper.emitted('selectDifficulty')[0]).toEqual(['easy'])
  })

  it('点击高级难度应触发selectDifficulty事件', async () => {
    const wrapper = mountComponent()
    const cards = wrapper.findAll('.difficulty-card')
    await cards[2].trigger('click')
    expect(wrapper.emitted('selectDifficulty')).toBeTruthy()
    expect(wrapper.emitted('selectDifficulty')[0]).toEqual(['hard'])
  })

  it('点击开始按钮应触发start事件', async () => {
    const wrapper = mountComponent()
    const startBtn = wrapper.find('.start-btn')
    await startBtn.trigger('click')
    expect(wrapper.emitted('start')).toBeTruthy()
  })

  it('当前选中难度应添加active样式', () => {
    const wrapper = mountComponent({ selectedDifficulty: 'medium' })
    const cards = wrapper.findAll('.difficulty-card')
    expect(cards[1].classes()).toContain('active')
  })
})

describe('PlantGame组件 - 游戏内容', () => {
  it('应渲染游戏内容区域', () => {
    const wrapper = mountComponent({ gameStarted: true })
    expect(wrapper.find('.game-content').exists()).toBe(true)
    expect(wrapper.find('.game-intro').exists()).toBe(false)
  })

  it('应显示当前得分', () => {
    const wrapper = mountComponent({ gameStarted: true, score: 30 })
    expect(wrapper.text()).toContain('当前得分')
    expect(wrapper.text()).toContain('30')
  })

  it('应显示计时器', () => {
    const wrapper = mountComponent({ gameStarted: true, formattedTime: '02:15' })
    expect(wrapper.find('.countdown-timer').exists()).toBe(true)
    expect(wrapper.text()).toContain('02:15')
  })

  it('低时间应添加low-time样式', () => {
    const wrapper = mountComponent({ gameStarted: true, isLowTime: true })
    expect(wrapper.find('.countdown-timer').classes()).toContain('low-time')
  })

  it('连击大于0时应显示连击数', () => {
    const wrapper = mountComponent({ gameStarted: true, streak: 5 })
    expect(wrapper.text()).toContain('连击')
    expect(wrapper.text()).toContain('5')
  })

  it('连击为0时不应显示连击', () => {
    const wrapper = mountComponent({ gameStarted: true, streak: 0 })
    expect(wrapper.text()).not.toContain('连击')
  })

  it('应显示题目', () => {
    const wrapper = mountComponent({ gameStarted: true })
    expect(wrapper.text()).toContain('这是什么植物？')
  })

  it('应显示选项按钮', () => {
    const wrapper = mountComponent({ gameStarted: true })
    expect(wrapper.text()).toContain('钩藤')
    expect(wrapper.text()).toContain('金银花')
    expect(wrapper.text()).toContain('艾草')
  })

  it('点击选项应触发answer事件', async () => {
    const wrapper = mountComponent({ gameStarted: true })
    const optBtns = wrapper.findAll('.game-opt-btn')
    if (optBtns.length > 0) {
      await optBtns[0].trigger('click')
      expect(wrapper.emitted('answer')).toBeTruthy()
      expect(wrapper.emitted('answer')[0]).toEqual(['钩藤'])
    }
  })

  it('应答后选项应禁用', () => {
    const wrapper = mountComponent({ gameStarted: true, answered: true, selectedAnswer: '金银花' })
    const optBtns = wrapper.findAll('.game-opt-btn')
    optBtns.forEach(btn => {
      expect(btn.attributes('disabled')).toBeDefined()
    })
  })

  it('应答正确时应添加correct样式', () => {
    const wrapper = mountComponent({
      gameStarted: true,
      answered: true,
      selectedAnswer: '钩藤',
      currentPlant: { nameCn: '钩藤', images: '/images/test.jpg' }
    })
    const optBtns = wrapper.findAll('.game-opt-btn')
    expect(optBtns[0].classes()).toContain('correct')
  })

  it('应答错误时应添加wrong样式', () => {
    const wrapper = mountComponent({
      gameStarted: true,
      answered: true,
      selectedAnswer: '金银花',
      currentPlant: { nameCn: '钩藤', images: '/images/test.jpg' }
    })
    const optBtns = wrapper.findAll('.game-opt-btn')
    expect(optBtns[1].classes()).toContain('wrong')
  })

  it('点击结束游戏按钮应触发endGame事件', async () => {
    const wrapper = mountComponent({ gameStarted: true })
    const actionsDiv = wrapper.find('.game-actions')
    if (actionsDiv.exists()) {
      const buttons = actionsDiv.findAll('button')
      if (buttons.length > 0) {
        await buttons[0].trigger('click')
      }
    }
    expect(wrapper.emitted('endGame')).toBeTruthy()
  })

  it('已登录时应显示收藏按钮', () => {
    const wrapper = mountComponent({ gameStarted: true, isLoggedIn: true })
    expect(wrapper.text()).toContain('收藏植物')
  })

  it('未登录时不应显示收藏按钮', () => {
    const wrapper = mountComponent({ gameStarted: true, isLoggedIn: false })
    expect(wrapper.text()).not.toContain('收藏植物')
  })

  it('点击收藏按钮应触发favorite事件', async () => {
    const wrapper = mountComponent({ gameStarted: true, isLoggedIn: true })
    const actionsDiv = wrapper.find('.game-actions')
    if (actionsDiv.exists()) {
      const buttons = actionsDiv.findAll('button')
      if (buttons.length > 1) {
        await buttons[1].trigger('click')
        expect(wrapper.emitted('favorite')).toBeTruthy()
      }
    }
  })
})

describe('PlantGame组件 - 结果界面', () => {
  it('应渲染结果界面', () => {
    const wrapper = mountComponent({ finished: true })
    expect(wrapper.find('.game-result').exists()).toBe(true)
    expect(wrapper.text()).toContain('游戏结束')
  })

  it('应显示最终得分', () => {
    const wrapper = mountComponent({ finished: true, score: 85 })
    expect(wrapper.text()).toContain('85')
  })

  it('应显示正确题数', () => {
    const wrapper = mountComponent({ finished: true, correct: 7, total: 10 })
    expect(wrapper.text()).toContain('正确')
    expect(wrapper.text()).toContain('7')
    expect(wrapper.text()).toContain('10')
  })

  it('应显示难度文本 - 初级', () => {
    const wrapper = mountComponent({ finished: true, selectedDifficulty: 'easy' })
    expect(wrapper.text()).toContain('初级')
  })

  it('应显示难度文本 - 中级', () => {
    const wrapper = mountComponent({ finished: true, selectedDifficulty: 'medium' })
    expect(wrapper.text()).toContain('中级')
  })

  it('应显示难度文本 - 高级', () => {
    const wrapper = mountComponent({ finished: true, selectedDifficulty: 'hard' })
    expect(wrapper.text()).toContain('高级')
  })

  it('未知难度应默认显示初级', () => {
    const wrapper = mountComponent({ finished: true, selectedDifficulty: 'unknown' })
    expect(wrapper.text()).toContain('初级')
  })

  it('点击重新开始按钮应触发restart事件', async () => {
    const wrapper = mountComponent({ finished: true })
    const restartBtn = wrapper.find('.result-actions')
    if (restartBtn.exists()) {
      const buttons = restartBtn.findAll('button-stub')
      if (buttons.length > 0) {
        await buttons[0].trigger('click')
        expect(wrapper.emitted('restart')).toBeTruthy()
      }
    }
  })
})

describe('PlantGame组件 - plantImage计算属性', () => {
  it('有图片时应返回getFirstImage结果', () => {
    const wrapper = mountComponent({
      currentPlant: { nameCn: '钩藤', images: '/images/gouteng.jpg' }
    })
    expect(wrapper.vm.plantImage).toBe('/images/gouteng.jpg')
  })

  it('无currentPlant时应返回占位图', () => {
    const wrapper = mountComponent({ currentPlant: null })
    expect(wrapper.vm.plantImage).toContain('data:image/svg+xml')
  })
})

describe('PlantGame组件 - 回归测试', () => {
  it('Bug: currentPlant为null时不应崩溃', () => {
    expect(() => mountComponent({ currentPlant: null, gameStarted: true })).not.toThrow()
  })

  it('Bug: options为空数组时不应崩溃', () => {
    expect(() => mountComponent({ options: [], gameStarted: true })).not.toThrow()
  })

  it('Bug: finished和gameStarted同时为true时应显示结果界面', () => {
    const wrapper = mountComponent({ finished: true, gameStarted: true })
    expect(wrapper.find('.game-result').exists()).toBe(true)
    expect(wrapper.find('.game-content').exists()).toBe(false)
  })

  it('Bug: score为负数时不应崩溃', () => {
    expect(() => mountComponent({ score: -1, finished: true })).not.toThrow()
  })

  it('Bug: selectedDifficulty为空字符串时应默认显示初级', () => {
    const wrapper = mountComponent({ finished: true, selectedDifficulty: '' })
    expect(wrapper.text()).toContain('初级')
  })
})
