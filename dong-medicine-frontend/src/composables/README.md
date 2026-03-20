# Composables目录

## 职责范围

组合式函数（Composables）用于封装和复用有状态的逻辑：

1. **逻辑复用**：提取可复用的状态逻辑
2. **状态封装**：封装响应式状态和相关方法
3. **关注点分离**：将复杂逻辑从组件中分离
4. **类型安全**：提供清晰的输入输出类型

## 文件列表

| 文件名 | 分类 | 功能描述 |
|--------|------|----------|
| useAdminData.js | 核心功能 | 管理后台数据获取与操作 |
| usePersonalCenter.js | 核心功能 | 个人中心功能 |
| useInteraction.js | 交互功能 | 交互相关（倒计时、评论、分页、过滤、统计） |
| useQuiz.js | 交互功能 | 趣味答题功能 |
| usePlantGame.js | 交互功能 | 植物识别游戏功能 |
| useMedia.js | 媒体功能 | 媒体相关（文档预览、媒体显示） |
| useFavorite.js | 通用功能 | 收藏功能 |
| useFormDialog.js | 通用功能 | 表单对话框 |
| useUpdateLog.js | 通用功能 | 更新日志 |

## 使用规范

### 导入方式

```javascript
// 推荐方式：从index.js导入
import { useInteraction, useMedia } from '@/composables'

// 或直接导入
import { useInteraction } from '@/composables/useInteraction'
```

### 使用示例

```javascript
import { useInteraction } from '@/composables'

export default {
  setup() {
    const { 
      countdown, 
      startCountdown,
      comments,
      fetchComments,
      pagination,
      filter
    } = useInteraction()
    
    return {
      countdown,
      startCountdown,
      comments,
      fetchComments,
      pagination,
      filter
    }
  }
}
```

## 开发规范

### Composable结构模板

```javascript
/**
 * @file useXxx.js
 * @description 功能描述
 * @author 作者
 * @version 1.0.0
 */

import { ref, computed, onMounted } from 'vue'
import { apiFunction } from '@/api'

/**
 * useXxx 组合式函数
 * @param {Object} options - 配置选项
 * @returns {Object} 暴露的状态和方法
 */
export function useXxx(options = {}) {
  // 响应式状态
  const data = ref(null)
  const loading = ref(false)
  const error = ref(null)
  
  // 计算属性
  const computedValue = computed(() => {
    // 计算逻辑
  })
  
  // 方法
  const fetchData = async () => {
    loading.value = true
    try {
      data.value = await apiFunction()
    } catch (e) {
      error.value = e
    } finally {
      loading.value = false
    }
  }
  
  // 生命周期
  onMounted(() => {
    fetchData()
  })
  
  // 暴露
  return {
    data,
    loading,
    error,
    computedValue,
    fetchData
  }
}
```

### 命名规范

- 文件名：use前缀 + 功能名，如useInteraction.js
- 函数名：与文件名一致，如useInteraction
- 返回值：对象解构，使用语义化命名

### 注意事项

1. 避免在Composable中直接操作DOM
2. 异步操作需要处理loading和error状态
3. 复杂逻辑可拆分为多个Composable
4. 保持Composable的单一职责

## 依赖关系

- 外部依赖：Vue 3 Composition API
- 内部依赖：Utils、API服务
- 被依赖：页面组件、业务组件
