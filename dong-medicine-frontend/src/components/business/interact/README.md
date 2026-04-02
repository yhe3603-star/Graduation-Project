# 交互组件目录 (interact)

本目录存放用户交互相关的组件，如评论、答题、游戏等。

## 组件列表

| 组件 | 功能描述 |
|------|----------|
| CaptchaInput.vue | 验证码输入组件 |
| CommentSection.vue | 评论组件，支持发表评论、回复 |
| InteractSidebar.vue | 互动侧边栏，展示排行榜和统计 |
| PlantGame.vue | 植物识别游戏 |
| QuizSection.vue | 趣味答题组件 |

---

## CommentSection.vue - 评论组件

支持发表评论、嵌套回复、点赞等功能。

```vue
<template>
  <div class="comment-section">
    <!-- 发表评论 -->
    <div class="comment-input">
      <textarea v-model="content" placeholder="发表评论..." />
      <button @click="submitComment">发表</button>
    </div>
    
    <!-- 评论列表 -->
    <div class="comment-list">
      <div v-for="comment in comments" :key="comment.id" class="comment-item">
        <div class="comment-user">{{ comment.username }}</div>
        <div class="comment-content">{{ comment.content }}</div>
        <div class="comment-actions">
          <button @click="replyTo(comment)">回复</button>
        </div>
        
        <!-- 嵌套回复 -->
        <div v-if="comment.replies" class="comment-replies">
          <!-- 子评论... -->
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
const props = defineProps({
  targetType: { type: String, required: true },
  targetId: { type: Number, required: true }
})

const emit = defineEmits(['submit', 'reply'])
</script>
```

---

## QuizSection.vue - 趣味答题

包含题目展示、选项选择、计时、评分等功能。

```vue
<template>
  <div class="quiz-section">
    <!-- 题目展示 -->
    <div class="quiz-question">
      <h3>{{ currentQuestion.question }}</h3>
      <div class="quiz-options">
        <button
          v-for="option in currentQuestion.options"
          :key="option"
          @click="selectAnswer(option)"
        >
          {{ option }}
        </button>
      </div>
    </div>
    
    <!-- 进度 -->
    <div class="quiz-progress">
      {{ currentIndex + 1 }} / {{ questions.length }}
    </div>
    
    <!-- 结果 -->
    <div v-if="showResult" class="quiz-result">
      得分: {{ score }}
    </div>
  </div>
</template>
```

---

## PlantGame.vue - 植物识别游戏

根据图片识别药材名称的游戏。

```vue
<template>
  <div class="plant-game">
    <!-- 植物图片 -->
    <img :src="currentPlant.image" class="plant-image">
    
    <!-- 选项 -->
    <div class="game-options">
      <button
        v-for="option in options"
        :key="option.id"
        @click="checkAnswer(option)"
      >
        {{ option.name }}
      </button>
    </div>
    
    <!-- 分数 -->
    <div class="game-score">
      得分: {{ score }}
    </div>
  </div>
</template>
```

---

**最后更新时间**：2026年4月3日
