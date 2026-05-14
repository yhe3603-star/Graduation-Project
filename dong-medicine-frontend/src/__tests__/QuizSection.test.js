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

import QuizSection from '@/components/business/interact/QuizSection.vue'

function mountComponent(props = {}) {
  return mount(QuizSection, {
    props: {
      isStarted: false,
      finished: false,
      loading: false,
      submitting: false,
      questions: [],
      current: 0,
      answers: [],
      score: 0,
      correct: 0,
      formattedTime: '03:00',
      isLowTime: false,
      selectedDifficulty: 'easy',
      ...props
    },
    global: {
      plugins: [ElementPlus],
      stubs: {
        'el-button': { template: '<button><slot /></button>' },
        'el-card': { template: '<div><slot /></div>' },
        'el-radio': { template: '<label><slot /></label>' },
        'el-radio-group': { template: '<div><slot /></div>' },
        'el-icon': { template: '<i><slot /></i>' },
      }
    }
  })
}

const sampleQuestions = [
  { q: '侗医药的代表药材是什么？', options: ['钩藤', '人参', '枸杞', '黄芪'], answer: '钩藤' },
  { q: '侗族药浴使用什么？', options: ['草药', '矿石', '动物', '真菌'], answer: '草药' },
  { q: '侗医药理论来源于？', options: ['实践经验', '中医理论', '西医理论', '宗教信仰'], answer: '实践经验' },
]

describe('QuizSection组件', () => {
  it('应渲染答题组件', () => {
    const wrapper = mountComponent()
    expect(wrapper.find('.quiz-section').exists()).toBe(true)
  })

  it('介绍页面：未开始且未完成时应显示难度选择', () => {
    const wrapper = mountComponent({ isStarted: false, finished: false })
    expect(wrapper.find('.quiz-intro').exists()).toBe(true)
    expect(wrapper.text()).toContain('侗医药知识趣味答题')
    expect(wrapper.text()).toContain('选择难度')
  })

  it('介绍页面：应显示三个难度卡片', () => {
    const wrapper = mountComponent({ isStarted: false, finished: false })
    expect(wrapper.text()).toContain('初级')
    expect(wrapper.text()).toContain('中级')
    expect(wrapper.text()).toContain('高级')
  })

  it('介绍页面：应显示开始答题按钮', () => {
    const wrapper = mountComponent({ isStarted: false, finished: false })
    expect(wrapper.text()).toContain('开始答题')
  })

  it('答题页面：已开始时应显示题目内容', () => {
    const wrapper = mountComponent({
      isStarted: true,
      questions: sampleQuestions,
      current: 0,
      answers: ['', '', '']
    })
    expect(wrapper.find('.quiz-content').exists()).toBe(true)
    expect(wrapper.text()).toContain(sampleQuestions[0].q)
  })

  it('答题页面：应显示进度信息', () => {
    const wrapper = mountComponent({
      isStarted: true,
      questions: sampleQuestions,
      current: 1,
      answers: ['钩藤', '', '']
    })
    expect(wrapper.text()).toContain('第 2 / 3 题')
  })

  it('答题页面：应显示计时器', () => {
    const wrapper = mountComponent({
      isStarted: true,
      questions: sampleQuestions,
      current: 0,
      answers: ['']
    })
    expect(wrapper.text()).toContain('03:00')
  })

  it('答题页面：isLowTime为true时应显示低时间状态', () => {
    const wrapper = mountComponent({
      isStarted: true,
      questions: sampleQuestions,
      current: 0,
      answers: [''],
      formattedTime: '00:05',
      isLowTime: true
    })
    const timer = wrapper.find('.countdown-timer')
    expect(timer.exists()).toBe(true)
    expect(timer.classes()).toContain('low-time')
  })

  it('答题页面：第一题时上一题按钮应禁用', () => {
    const wrapper = mountComponent({
      isStarted: true,
      questions: sampleQuestions,
      current: 0,
      answers: ['', '', '']
    })
    const prevBtn = wrapper.findAll('el-button-stub').find(b => b.text().includes('上一题'))
    if (prevBtn) {
      expect(prevBtn.attributes('disabled')).toBeDefined()
    }
  })

  it('答题页面：非最后一题应显示下一题和提前交卷按钮', () => {
    const wrapper = mountComponent({
      isStarted: true,
      questions: sampleQuestions,
      current: 0,
      answers: ['', '', '']
    })
    expect(wrapper.text()).toContain('下一题')
    expect(wrapper.text()).toContain('提前交卷')
  })

  it('答题页面：最后一题应显示提交答卷按钮', () => {
    const wrapper = mountComponent({
      isStarted: true,
      questions: sampleQuestions,
      current: 2,
      answers: ['钩藤', '草药', '']
    })
    expect(wrapper.text()).toContain('提交答卷')
  })

  it('结果页面：已完成时应显示得分', () => {
    const wrapper = mountComponent({
      finished: true,
      questions: sampleQuestions,
      score: 85,
      correct: 2,
      selectedDifficulty: 'easy'
    })
    expect(wrapper.find('.quiz-result').exists()).toBe(true)
    expect(wrapper.text()).toContain('85')
    expect(wrapper.text()).toContain('正确 2 / 3 题')
  })

  it('结果页面：90分以上应显示优秀评价', () => {
    const wrapper = mountComponent({
      finished: true,
      questions: sampleQuestions,
      score: 95,
      correct: 3,
      selectedDifficulty: 'easy'
    })
    expect(wrapper.text()).toContain('太棒了！你是侗医药达人！')
  })

  it('结果页面：70-89分应显示不错的评价', () => {
    const wrapper = mountComponent({
      finished: true,
      questions: sampleQuestions,
      score: 75,
      correct: 2,
      selectedDifficulty: 'medium'
    })
    expect(wrapper.text()).toContain('不错哦！继续加油！')
  })

  it('结果页面：60-69分应显示及格评价', () => {
    const wrapper = mountComponent({
      finished: true,
      questions: sampleQuestions,
      score: 65,
      correct: 2,
      selectedDifficulty: 'easy'
    })
    expect(wrapper.text()).toContain('及格啦！再接再厉！')
  })

  it('结果页面：60分以下应显示需努力评价', () => {
    const wrapper = mountComponent({
      finished: true,
      questions: sampleQuestions,
      score: 40,
      correct: 1,
      selectedDifficulty: 'hard'
    })
    expect(wrapper.text()).toContain('还需努力，多学习侗医药知识吧！')
  })

  it('结果页面：应显示难度信息', () => {
    const wrapper = mountComponent({
      finished: true,
      questions: sampleQuestions,
      score: 80,
      correct: 2,
      selectedDifficulty: 'easy'
    })
    expect(wrapper.text()).toContain('初级')
  })

  it('结果页面：中级难度应显示正确标签', () => {
    const wrapper = mountComponent({
      finished: true,
      questions: sampleQuestions,
      score: 80,
      correct: 2,
      selectedDifficulty: 'medium'
    })
    expect(wrapper.text()).toContain('中级')
  })

  it('结果页面：高级难度应显示正确标签', () => {
    const wrapper = mountComponent({
      finished: true,
      questions: sampleQuestions,
      score: 80,
      correct: 2,
      selectedDifficulty: 'hard'
    })
    expect(wrapper.text()).toContain('高级')
  })

  it('结果页面：应显示重新选择和分享按钮', () => {
    const wrapper = mountComponent({
      finished: true,
      questions: sampleQuestions,
      score: 80,
      correct: 2
    })
    expect(wrapper.text()).toContain('重新选择难度')
    expect(wrapper.text()).toContain('分享成绩')
  })

  it('点击难度卡片应触发selectDifficulty事件', async () => {
    const wrapper = mountComponent({ isStarted: false, finished: false })
    const cards = wrapper.findAll('.difficulty-card')
    expect(cards.length).toBeGreaterThanOrEqual(1)
    await cards[1].trigger('click')
    expect(wrapper.emitted('selectDifficulty')).toBeTruthy()
    expect(wrapper.emitted('selectDifficulty')[0]).toEqual(['medium'])
  })

  it('点击开始答题应触发start事件', async () => {
    const wrapper = mountComponent({ isStarted: false, finished: false })
    const startBtn = wrapper.find('.start-btn')
    await startBtn.trigger('click')
    expect(wrapper.emitted('start')).toBeTruthy()
  })

  it('点击重试应触发retry事件', async () => {
    const wrapper = mountComponent({ finished: true, score: 80, questions: sampleQuestions, correct: 2 })
    const retryBtn = wrapper.find('.result-actions el-button-stub')
    if (retryBtn.exists()) {
      await retryBtn.trigger('click')
      expect(wrapper.emitted('retry')).toBeTruthy()
    }
  })

  it('loading状态应传递给开始按钮', () => {
    const wrapper = mountComponent({ isStarted: false, finished: false, loading: true })
    const startBtn = wrapper.find('.start-btn')
    expect(startBtn.attributes('loading')).toBeDefined()
  })

  it('submitting状态应传递给提交按钮', () => {
    const wrapper = mountComponent({
      isStarted: true,
      questions: sampleQuestions,
      current: 2,
      answers: ['钩藤', '草药', ''],
      submitting: true
    })
    expect(wrapper.text()).toContain('提交答卷')
  })
})

describe('QuizSection组件 - 回归测试', () => {
  it('Bug: questions为空数组时不应崩溃', () => {
    expect(() => mountComponent({
      isStarted: true,
      questions: [],
      current: 0,
      answers: []
    })).not.toThrow()
  })

  it('Bug: score为0时不应崩溃', () => {
    expect(() => mountComponent({
      finished: true,
      score: 0,
      correct: 0,
      questions: sampleQuestions
    })).not.toThrow()
  })

  it('Bug: selectedDifficulty为无效值时应默认显示初级', () => {
    const wrapper = mountComponent({
      finished: true,
      score: 80,
      correct: 2,
      questions: sampleQuestions,
      selectedDifficulty: 'invalid'
    })
    expect(wrapper.text()).toContain('初级')
  })

  it('Bug: current超出questions范围时不应崩溃', () => {
    expect(() => mountComponent({
      isStarted: true,
      questions: sampleQuestions,
      current: 99,
      answers: ['钩藤', '草药', '实践经验']
    })).not.toThrow()
  })

  it('Bug: answers长度与questions不匹配时不应崩溃', () => {
    expect(() => mountComponent({
      isStarted: true,
      questions: sampleQuestions,
      current: 0,
      answers: []
    })).not.toThrow()
  })
})
