# 互动组件（Interact Components）

## 什么是互动组件？

类比：**互动游戏区**——博物馆里不只是看展品，还有体验区让你动手操作、参与互动。

互动组件让用户不只是"看"，还能"做"——发表评论、回答问题、玩小游戏、输入验证码。它们是平台中最有趣的部分，能大幅提升用户参与度。

```
展示组件：用户看 → "哦，知道了"
互动组件：用户做 → "哇，真有趣！" ← 参与感更强
```

---

## 组件列表

### CommentSection —— 评论区

**用途：** 让用户对药用植物、知识条目等内容发表评论和回复

```
┌────────────────────────────────────┐
│  💬 评论（12条）                    │
│                                    │
│  ┌──────────────────────────────┐  │
│  │ 用户A：钩藤泡茶效果不错！      │  │
│  │        2025-04-20  回复 👍 3  │  │
│  │  └ 用户B：请问怎么泡？        │  │
│  └──────────────────────────────┘  │
│                                    │
│  [写下你的评论...]        [发表]   │
└────────────────────────────────────┘
```

```vue
<!-- 使用示例 -->
<CommentSection
  :comments="commentList"
  :content-id="plantId"
  :content-type="'plant'"
  @submit-comment="handleSubmitComment"
/>
```

---

### QuizSection —— 知识问答区

**用途：** 展示侗医药知识测验题目，用户选择答案后即时反馈对错

```
┌────────────────────────────────────┐
│  📝 侗医药知识测验                  │
│                                    │
│  第 3 题 / 共 10 题                │
│                                    │
│  问题：钩藤的主要功效是什么？       │
│                                    │
│  ○ A. 清热解毒                     │
│  ● B. 息风止痉  ← 你选了这个       │
│  ○ C. 活血化瘀                     │
│  ○ D. 补气养阴                     │
│                                    │
│  ✅ 回答正确！钩藤具有息风止痉、     │
│     清热平肝的功效。                │
│                                    │
│              [下一题 →]             │
└────────────────────────────────────┘
```

```vue
<!-- 使用示例 -->
<QuizSection
  :quiz-data="currentQuiz"
  :current-index="quizIndex"
  @answer="handleAnswer"
  @next="handleNext"
/>
```

---

### PlantGame —— 药用植物识别游戏

**用途：** 通过游戏方式帮助用户学习识别侗族药用植物

```
┌────────────────────────────────────┐
│  🎮 药用植物识别挑战                │
│                                    │
│  ┌──────────────────────────┐      │
│  │                          │      │
│  │    [显示一张植物图片]      │      │
│  │                          │      │
│  └──────────────────────────┘      │
│                                    │
│  这是什么植物？                     │
│                                    │
│  [钩藤]  [透骨草]  [九节茶]  [半夏] │
│                                    │
│  得分：80    时间：00:30            │
└────────────────────────────────────┘
```

```vue
<!-- 使用示例 -->
<PlantGame
  :plants="gamePlants"
  :difficulty="'medium'"
  @game-over="handleGameOver"
/>
```

---

### CaptchaInput —— 验证码输入

**用途：** 在提交评论、注册登录等场景中验证用户身份，防止机器人刷屏

```
┌────────────────────────────────────┐
│  请输入验证码：                     │
│                                    │
│  ┌───┐ ┌───┐ ┌───┐ ┌───┐         │
│  │ 3 │ │ 7 │ │ _ │ │ _ │         │
│  └───┘ └───┘ └───┘ └───┘         │
│                                    │
│  [A x K 2]  🔄 换一张              │
│  ↑ 图形验证码                      │
└────────────────────────────────────┘
```

```vue
<!-- 使用示例 -->
<CaptchaInput
  :length="4"
  @verify="handleCaptchaVerify"
  @refresh="handleCaptchaRefresh"
/>
```

---

### InteractSidebar —— 互动侧边栏

**用途：** 在互动页面侧边展示快捷操作入口和互动统计

```
┌──────────┬─────────────────────┐
│ 🎮 互动   │                     │
│          │                     │
│ · 知识测验 │   主内容区域        │
│ · 植物游戏 │                     │
│ · 我的评论 │                     │
│          │                     │
│ 📊 统计   │                     │
│ 答题：42题│                     │
│ 正确率：85%│                    │
└──────────┴─────────────────────┘
```

```vue
<!-- 使用示例 -->
<InteractSidebar
  :stats="userStats"
  :active-key="currentInteract"
  @nav-click="handleNavClick"
/>
```

---

## 常见错误

### 错误1：评论提交后没有更新列表

```vue
<script setup>
// ❌ 提交评论后没有刷新评论列表，用户看不到自己发的评论
const submitComment = async (content) => {
  await api.postComment(content)
  // 缺少刷新列表的代码！
}

// ✅ 提交后重新获取评论列表
const submitComment = async (content) => {
  await api.postComment(content)
  // 重新加载评论列表
  await loadComments()
}
</script>
```

### 错误2：游戏没有处理退出时的清理

```vue
<script setup>
import { onMounted, onUnmounted } from 'vue'

// ❌ 游戏的定时器在组件销毁时没有清除，导致内存泄漏
let timer = null
onMounted(() => {
  timer = setInterval(() => { /* 倒计时 */ }, 1000)
})
// 缺少 onUnmounted 清理！

// ✅ 组件销毁时清除定时器
onUnmounted(() => {
  if (timer) {
    clearInterval(timer)
    timer = null
  }
})
</script>
```

### 错误3：验证码验证放在前端

```vue
<script setup>
// ❌ 验证码的正确答案放在前端，用户打开开发者工具就能看到
const correctCaptcha = '3K2A'  // 暴露了！

// ✅ 验证码只能由后端验证，前端只负责收集用户输入
const verifyCaptcha = async (userInput) => {
  const res = await api.verifyCaptcha({ code: userInput })
  // 由后端判断对错
  return res.data.valid
}
</script>
```

---

## 代码审查与改进建议

- [安全] CommentSection.vue显示500字符限制但el-input未设置maxlength属性
- [组件设计] CommentSection.vue通过emit传递成功/失败回调函数是Vue反模式
- [错误处理] CaptchaInput.vue验证码加载失败无用户提示
- [输入验证] SearchFilter.vue手动实现防抖而非使用已有的useDebounceFn composable
